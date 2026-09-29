package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.RecursoDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionManager;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecursoDAOImpl implements RecursoDAO {
    @Override
    public List<Recurso> findAll() throws SQLException {
        String sql = """
                    SELECT id,nombre,tipo FROM recurso
                """;
        try(Connection conn = DBManager.getInstance().getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql);
            ResultSet rs = cmd.executeQuery()){
            List<Recurso> recursos= new ArrayList<>();
            while (rs.next()){
                recursos.add (mapear(rs,new Recurso()));
            }
            return recursos;
        }catch(SQLException ex){
            throw new SQLException("",ex);
        }
    }

    @Override
    public Recurso findById(Integer id) throws SQLException {
        //parche contra id=null
        if(id==null){
            throw new RuntimeException("el id no puede ser nulo");
        }
        //igual que findAll: sql + try(conn + conn.Pps(sql))
        String sql = """
                SELECT id,nombre,tipo,activo FROM recurso
                WHERE id=?
                """;
        try(Connection conn = DBManager.getInstance().getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql)){
            //se corta antes del rs pq cmd.setInt -> void :NO ES AUTOCLOSABLE
            cmd.setInt(1,id);
            //otro try para terminar
            try(ResultSet rs = cmd.executeQuery()){
                return !rs.next()? null:mapear(rs,new Recurso());
            }catch(SQLException e){
                throw new SQLException("",e);
            }

        }catch(SQLException ex){
            throw new SQLException("No se pudo establecer la coneccion",ex);
        }
    }

    @Override
    public void insert(Recurso modelo) throws SQLException {
<<<<<<< Updated upstream
        if(modelo==null)  throw new IllegalArgumentException("El recurso no puede ser nulo");

        String sql = """
                INSERT INTO recurso(nombre,tipo,activo)  
                VALUES (?,?,?)
                """;
        try(Connection conn = DBManager.getInstance().getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS)){
            cmd.setString(1,modelo.getNombre());
            cmd.setString(2,modelo.getTipo());
            cmd.setBoolean(3,modelo.estaActivo());

            if(cmd.executeUpdate()==0) throw new  SQLException("No se pudo insertar");

            try(ResultSet keys = cmd.getGeneratedKeys()){
                if(!keys.next()) throw new SQLException("No se obtuvo el ID");
                modelo.setId(keys.getInt(1));
            }
        }catch (SQLException e){
            throw new SQLException("",e);
        }
=======
        String sql = """
                SELECT id,nombre,tipo FROM recurso
                WHERE id=?
                """;
>>>>>>> Stashed changes

        try(Connection conn = TransactionManager.getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql);
            ResultSet rs = cmd.executeQuery()
        ){

        }
    }

    @Override
    public void update(Recurso modelo) throws SQLException {
        if(modelo==null)  throw new IllegalArgumentException("El recurso no puede ser nulo");
        String sql = """
                UPDATE recurso 
                SET nombre=?,tipo=?,activo=?
                WHERE id=?
                           """;
        try(Connection conn = DBManager.getInstance().getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql)){
            cmd.setInt(1,modelo.getId());
            cmd.setString(2,modelo.getNombre());
            cmd.setString(3,modelo.getTipo());
            cmd.setBoolean(4,modelo.estaActivo());

            if(cmd.executeUpdate()==0) throw new  SQLException("No se pudo insertar");
        }catch (SQLException e){
            throw new SQLException("",e);
        }

    }

    @Override
    public void delete(Integer id) throws SQLException {
        if(id==null)  throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = """
                DELETE FROM recurso 
                            WHERE id=?
                           """;
        try(Connection conn = DBManager.getInstance().getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql)){
            cmd.setInt(1,id);
            if(cmd.executeUpdate()==0) throw new  SQLException("No se pudo eliminar");

        }catch (SQLException e){
            throw new SQLException("",e);
        }
    }
    private Recurso mapear(ResultSet rs,Recurso recurso) throws SQLException {
        recurso.setId(rs.getInt("id"));
        recurso.setNombre(rs.getString("nombre"));
        recurso.setTipo(rs.getString("tipo"));
        return recurso;
    }
}
