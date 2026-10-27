package pe.edu.pucp.entrefunciones.dao.transacciones;

import pe.edu.pucp.entrefunciones.db.DBManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionsManager {
    private static final ThreadLocal<EstadoTransaccion> ESTADO = new ThreadLocal<>();

    private TransactionsManager() {
    }

    public static void iniciar() {
        if (activa()) {
            throw new IllegalStateException("Ya existe una transacción activa en este hilo");
        }
        Connection connection = null;
        try {
            connection = DBManager.getInstance().getConnection();
            iniciar(connection);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeError) {
                    e.addSuppressed(closeError);
                }
            }
            throw new TransactionException("No se pudo iniciar la transacción", e);
        }
    }

    public static void iniciar(Connection connection) {
        if (activa()) {
            throw new IllegalStateException("Ya existe una transacción activa en este hilo");
        }
        if (connection == null) {
            throw new IllegalArgumentException("La conexión de la transacción no puede ser nula");
        }

        try {
            connection.setAutoCommit(false);
            ESTADO.set(new EstadoTransaccion(connection));
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw new TransactionException("No se pudo configurar la transacción", e);
        }
    }

    public static void commit() {
        EstadoTransaccion estado = exigirEstado();
        if (estado.rollbackOnly) {
            TransactionException error = new TransactionException(
                    "La transacción está marcada para reversión");
            revertirYCerrar(estado, error);
            throw error;
        }

        Throwable error = null;
        try {
            estado.connection.commit();
        } catch (SQLException commitError) {
            error = new TransactionException("No se pudo confirmar la transacción", commitError);
            try {
                estado.connection.rollback();
            } catch (SQLException rollbackError) {
                error.addSuppressed(rollbackError);
            }
        } finally {
            error = cerrar(estado, error);
        }

        if (error != null) {
            throw comoTransactionException(error, "No se pudo finalizar la transacción");
        }
    }

    public static void rollback() {
        EstadoTransaccion estado = exigirEstado();
        Throwable error = null;
        try {
            estado.connection.rollback();
        } catch (SQLException rollbackError) {
            error = new TransactionException("No se pudo revertir la transacción", rollbackError);
        } finally {
            error = cerrar(estado, error);
        }

        if (error != null) {
            throw comoTransactionException(error, "No se pudo finalizar la reversión");
        }
    }

    public static Connection getConnection() {
        return exigirEstado().connection;
    }

    public static boolean activa() {
        return ESTADO.get() != null;
    }

    public static void marcarRollbackOnly() {
        exigirEstado().rollbackOnly = true;
    }

    public static boolean estaMarcadaRollbackOnly() {
        EstadoTransaccion estado = ESTADO.get();
        return estado != null && estado.rollbackOnly;
    }

    private static EstadoTransaccion exigirEstado() {
        EstadoTransaccion estado = ESTADO.get();
        if (estado == null) {
            throw new IllegalStateException("No hay una transacción activa en este hilo");
        }
        return estado;
    }

    private static void revertirYCerrar(EstadoTransaccion estado, Throwable error) {
        try {
            estado.connection.rollback();
        } catch (SQLException rollbackError) {
            error.addSuppressed(rollbackError);
        } finally {
            cerrar(estado, error);
        }
    }

    private static Throwable cerrar(EstadoTransaccion estado, Throwable error) {
        ESTADO.remove();
        try {
            estado.connection.close();
        } catch (SQLException closeError) {
            if (error == null) {
                return new TransactionException(
                        "No se pudo cerrar la conexión de la transacción", closeError);
            }
            error.addSuppressed(closeError);
        }
        return error;
    }

    private static TransactionException comoTransactionException(Throwable error, String mensaje) {
        if (error instanceof TransactionException transactionException) {
            return transactionException;
        }
        return new TransactionException(mensaje, error);
    }

    private static final class EstadoTransaccion {
        private final Connection connection;
        private boolean rollbackOnly;

        private EstadoTransaccion(Connection connection) {
            this.connection = connection;
        }
    }

    public static final class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }

        public TransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
