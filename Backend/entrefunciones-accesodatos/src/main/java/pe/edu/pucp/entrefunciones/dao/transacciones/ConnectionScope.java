package pe.edu.pucp.entrefunciones.dao.transacciones;

import pe.edu.pucp.entrefunciones.db.DBManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionScope implements AutoCloseable {
    private final Connection connection;
    private final boolean owner;

    private ConnectionScope(Connection connection, boolean owner) {
        this.connection = connection;
        this.owner = owner;
    }

    public static ConnectionScope openForRead() throws SQLException {
        if (TransactionsManager.activa()) {
            return new ConnectionScope(TransactionsManager.getConnection(), false);
        }
        return new ConnectionScope(DBManager.getInstance().getConnection(), true);
    }

    public Connection connection() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        if (owner) {
            connection.close();
        }
    }
}
