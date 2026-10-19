package pe.edu.pucp.entrefunciones.bl.impl;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;

import java.sql.SQLException;

/**
 * Utilidad compartida de todo el modulo entrefunciones-negocio (no solo de
 * "espacios", que es donde nacio) para ejecutar una operacion de negocio
 * compuesta por varias escrituras DAO como una unica transaccion, siguiendo
 * el patron del profesor (ver softprog OrdenVentaBLImpl: "la BL es quien
 * inicia, confirma o revierte") adaptado con soporte de transacciones
 * anidadas: si ya hay una transaccion activa en el hilo (por ejemplo, un
 * BL que a su vez llama a otro BL), esta clase NO abre una segunda; se
 * suma a la existente y, si algo falla, la marca "rollbackOnly" en vez de
 * revertir una transaccion de la que no es dueña (ver
 * TransactionsManager.marcarRollbackOnly()).
 *
 * <p>Cualquier BLImpl de cualquier paquete (espacios, eventos, ventas,
 * usuarios) usa esta clase de la misma forma:
 * {@code TransaccionBL.ejecutar("mensaje si falla", () -> { ...llamadas a DAO...; })}.
 * Los DAOImpl involucrados deben, a su vez, escribir con
 * {@code TransactionsManager.getConnection()} (nunca abrir su propia
 * conexion independiente) para participar de la misma transaccion —
 * exactamente como ya hacen BloqueoDAOImpl/SedeDAOImpl/ButacaDAOImpl.</p>
 */
public final class TransaccionBL {
    private TransaccionBL() {
    }

    public static void ejecutar(String mensajeError, Operacion operacion) throws BLException {
        boolean propietaria = !TransactionsManager.activa();
        if (propietaria) {
            try {
                TransactionsManager.iniciar();
            } catch (RuntimeException e) {
                throw new BLException("No se pudo iniciar la transacción", e);
            }
        }

        try {
            operacion.ejecutar();
            if (propietaria) {
                TransactionsManager.commit();
            }
        } catch (BLException e) {
            manejarFallo(propietaria, e);
            throw e;
        } catch (SQLException e) {
            BLException error = new BLException(mensajeError, e);
            manejarFallo(propietaria, error);
            throw error;
        } catch (RuntimeException e) {
            BLException error = new BLException(mensajeError, e);
            manejarFallo(propietaria, error);
            throw error;
        }
    }

    private static void manejarFallo(boolean propietaria, BLException error) {
        if (!TransactionsManager.activa()) {
            return;
        }
        if (!propietaria) {
            TransactionsManager.marcarRollbackOnly();
            return;
        }
        try {
            TransactionsManager.rollback();
        } catch (RuntimeException rollbackError) {
            error.addSuppressed(rollbackError);
        }
    }

    @FunctionalInterface
    public interface Operacion {
        void ejecutar() throws BLException, SQLException;
    }
}
