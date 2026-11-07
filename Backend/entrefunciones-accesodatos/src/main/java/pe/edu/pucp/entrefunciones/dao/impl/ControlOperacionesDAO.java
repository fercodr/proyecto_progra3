package pe.edu.pucp.entrefunciones.dao.impl;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
/** Las mutaciones de ventas se serializan por función; la programación, por sala.
 * Se exige la transacción del BL. Orden: función -> reserva -> pago. */
public class ControlOperacionesDAO {
    private Connection conn() { return TransactionsManager.getConnection(); }
    public void bloquearFuncion(int id) throws SQLException { bloquear("funcion", id); }
    public void bloquearSala(int id) throws SQLException { bloquear("sala", id); }
    public void bloquearReserva(int id) throws SQLException { bloquear("reserva", id); }
    public void bloquearPago(int id) throws SQLException { bloquear("pago", id); }
    private void bloquear(String tabla, int id) throws SQLException {
        // tabla solo proviene de las constantes privadas anteriores.
        try (PreparedStatement q = conn().prepareStatement("SELECT id FROM " + tabla + " WHERE id=? FOR UPDATE")) {
            q.setInt(1,id); try(ResultSet rs=q.executeQuery()) {
                if (!rs.next()) throw new SQLException("No existe " + tabla + " " + id);
            }
        }
    }
    public int funcionDeReserva(int id) throws SQLException { return entero("SELECT funcion_id FROM reserva WHERE id=?", id); }
    public int reservaDePago(int id) throws SQLException { return entero("SELECT reserva_id FROM pago WHERE id=?", id); }
    private int entero(String sql, int id) throws SQLException {
        try(PreparedStatement q=conn().prepareStatement(sql)) { q.setInt(1,id);
            try(ResultSet rs=q.executeQuery()) { if(!rs.next()) throw new SQLException("No existe el registro " + id); return rs.getInt(1); }
        }
    }
    public LocalDateTime ahora() throws SQLException {
        try(Statement q=conn().createStatement();ResultSet rs=q.executeQuery("SELECT CURRENT_TIMESTAMP")) { rs.next(); return rs.getTimestamp(1).toLocalDateTime(); }
    }
    public void expirarEnFuncion(int id) throws SQLException {
        try(PreparedStatement q=conn().prepareStatement("UPDATE reserva SET estado='EXPIRADA' WHERE funcion_id=? AND estado='ACTIVA' AND fecha_expiracion<=CURRENT_TIMESTAMP")) {
            q.setInt(1,id);q.executeUpdate();
        }
        try(PreparedStatement q=conn().prepareStatement("UPDATE pago p JOIN reserva r ON r.id=p.reserva_id SET p.estado='RECHAZADO' WHERE r.funcion_id=? AND r.estado IN ('EXPIRADA','CANCELADA') AND p.estado='PENDIENTE'")) {
            q.setInt(1,id);q.executeUpdate();
        }
    }
    public Set<Integer> butacasOcupadas(int funcion) throws SQLException {
        Set<Integer> ids=new HashSet<>();
        try(PreparedStatement q=conn().prepareStatement("SELECT rb.butaca_id FROM reserva_butaca rb JOIN reserva r ON r.id=rb.reserva_id WHERE r.funcion_id=? AND (r.estado='CONFIRMADA' OR (r.estado='ACTIVA' AND r.fecha_expiracion>CURRENT_TIMESTAMP))")) {
            q.setInt(1,funcion);try(ResultSet rs=q.executeQuery()){while(rs.next()) ids.add(rs.getInt(1));}
        } return ids;
    }
    public int cuposOcupados(int funcion) throws SQLException {
        return entero("SELECT COALESCE(SUM(cantidad),0) FROM reserva WHERE funcion_id=? AND (estado='CONFIRMADA' OR (estado='ACTIVA' AND fecha_expiracion>CURRENT_TIMESTAMP))", funcion);
    }
    public boolean tieneReservas(int funcion) throws SQLException {
        return entero("SELECT COUNT(*) FROM reserva WHERE funcion_id=? AND (estado='CONFIRMADA' OR (estado='ACTIVA' AND fecha_expiracion>CURRENT_TIMESTAMP))", funcion)>0;
    }
    public List<Integer> funcionesPorExpirar() throws SQLException {
        try(ConnectionScope scope=ConnectionScope.openForRead(); PreparedStatement q=scope.connection().prepareStatement("SELECT DISTINCT funcion_id FROM reserva WHERE estado='ACTIVA' AND fecha_expiracion<=CURRENT_TIMESTAMP");ResultSet rs=q.executeQuery()) {
            List<Integer> ids=new ArrayList<>();while(rs.next())ids.add(rs.getInt(1));return ids;
        }
    }
}
