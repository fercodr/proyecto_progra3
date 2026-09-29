package pe.edu.pucp.entrefunciones.dao.transacciones;

import pe.edu.pucp.entrefunciones.db.DBManager;

import java.sql.Connection;
import java.sql.SQLException;

//final para evitar duplicados
public final class TransactionManager {

    //solo acceso privado
    private  static final ThreadLocal<Connection> thead= new ThreadLocal<>();
    //constructor privado !
    private TransactionManager(){
    }
//DEMAS METODOS STATIC

    public void start() throws SQLException {
        if (active()) throw new RuntimeException("ya existe un hijo activo");

        Connection conn = null;
        try {
            conn = DBManager.getInstance().getConnection();
            //devuelven void
            conn.setAutoCommit(false);
            thead.set(conn);
        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
            throw new RuntimeException("No se pudo iniciar la transaccion", ex);
        }
    }
    public static Connection getConnection(){
        Connection conn = thead.get();
        if(conn==null) throw new RuntimeException("");
        return conn;
    }

    public static void commit() {
        Connection conn = getConnection();
        try {
            conn.commit();
        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) { }
            throw new RuntimeException("No se pudo confirmar la transaccion", ex);
        } finally {
            cerrar(conn);
        }
    }

    public static void rollback() {
        Connection conn = getConnection();
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo revertir la transaccion", ex);
        } finally {
            cerrar(conn);
        }
    }
    public static boolean active(){
        return thead.get()!=null;
    }
    private static void cerrar(Connection conn) {
        thead.remove();
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ex) {
            throw new RuntimeException("No se pudo cerrar la conexion", ex);
        }
    }

}
