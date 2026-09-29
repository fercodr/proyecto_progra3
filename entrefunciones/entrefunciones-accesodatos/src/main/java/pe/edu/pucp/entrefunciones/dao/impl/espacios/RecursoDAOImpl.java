package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.RecursoDAO;
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
                SELECT id,nombre,tipo FROM recurso
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

    }

    @Override
    public void update(Recurso modelo) throws SQLException {

    }

    @Override
    public void delete(Integer integer) throws SQLException {

    }
    private Recurso mapear(ResultSet rs,Recurso recurso) throws SQLException {
        recurso.setId(rs.getInt("id"));
        recurso.setNombre(rs.getString("nombre"));
        recurso.setTipo(rs.getString("tipo"));
        return recurso;
    }
}
