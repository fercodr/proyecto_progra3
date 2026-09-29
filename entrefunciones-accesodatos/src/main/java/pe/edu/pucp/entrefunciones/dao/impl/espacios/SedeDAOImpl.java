package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SedeDAOImpl implements SedeDAO {

    @Override
    public List<Sede> findAll() throws SQLException {
        //PASO 1 GENERAR SENTENCIA SQL
        String sql = """
                SELECT id,razon_social,direccion,ciudad,telefono_contacto
                FROM sede
                """;
        //PASO 2:TRY CATCH Abrir coneccion + asocioar conn->sql + rs= executeQuery
        try(Connection conn = DBManager.getInstance().getConnection();
        PreparedStatement cmd = conn.prepareStatement(sql);
            ResultSet rs = cmd.executeQuery()){
        //PASO 3: CREAMOS EL OBJETO A LLENAR COMO ARRAYLIST +
            List<Sede> sedes = new ArrayList<>();
        //LENAMOS LOS CAMPOS Y AVANZAMOS CON "rs.next()"
            while(rs.next()) {
                sedes.add(mapear(rs,new Sede()));
            }
            return sedes;
        }catch(SQLException e){
            throw new SQLException("",e);
        }

    }

    @Override
    public Sede findById(Integer integer) throws SQLException {
        return null;
    }

    @Override
    public void insert(Sede modelo) throws SQLException {

    }

    @Override
    public void update(Sede modelo) throws SQLException {

    }

    @Override
    public void delete(Integer integer) throws SQLException {

    }
    private Sede mapear(ResultSet rs, Sede sede)throws SQLException{
        sede.setId(rs.getInt("id"));
        sede.setRazonSocial(rs.getString("razon_social"));
        sede.setDireccion(rs.getString("direccion"));
        sede.setCiudad(rs.getString("ciudad"));
        sede.setTelefonoContacto(rs.getString("telefono_contacto"));
        return sede;
    }
}
