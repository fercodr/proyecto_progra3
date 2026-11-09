package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;

import java.sql.SQLException;

final class TransaccionBL {
    private TransaccionBL() {
    }

    static void ejecutar(String mensajeError, Operacion operacion) throws BLException {
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
    interface Operacion {
        void ejecutar() throws BLException, SQLException;
    }
}
