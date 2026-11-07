package pe.edu.pucp.entrefunciones.bl;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import java.sql.SQLException;
public final class Transacciones {
    private Transacciones() { }
    @FunctionalInterface public interface Operacion<T> { T ejecutar() throws SQLException, BLException; }
    public static <T> T ejecutar(String mensaje, Operacion<T> operacion) throws BLException {
        boolean propia = !TransactionsManager.activa();
        try {
            if (propia) TransactionsManager.iniciar();
            T resultado = operacion.ejecutar();
            if (propia) TransactionsManager.commit();
            return resultado;
        } catch (SQLException | RuntimeException | BLException e) {
            if (TransactionsManager.activa()) {
                if (!propia) TransactionsManager.marcarRollbackOnly();
                else try { TransactionsManager.rollback(); } catch (RuntimeException ex) { e.addSuppressed(ex); }
            }
            if (e instanceof BLException be) throw be;
            throw new BLException(mensaje, e);
        }
    }
}
