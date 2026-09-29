package pe.edu.pucp.entrefunciones.dao.Impl;

import pe.edu.pucp.entrefunciones.dao.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.SedeDAO;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalaDAOImpl implements SalaDAO {
    @Override
    public List<Sala> findAll() throws SQLException {
        String sql = """
                SELECT * FROM sala
                """;
        try(Connection conn = DBManager.getInstance().getConnection();
            PreparedStatement cmd = conn.prepareStatement(sql);
            ResultSet rs = cmd.executeQuery()){
            List<Sala> salas = new ArrayList<>();
            while(rs.next()){
             salas.add(mapear(rs,new Sala()));
            }

        }catch (SQLException e){
            throw new SQLException("",e);
        }
    }

    @Override
    public Sala findById(Integer integer) throws SQLException {
        return null;
    }

    @Override
    public void insert(Sala modelo) throws SQLException {

    }

    @Override
    public void update(Sala modelo) throws SQLException {

    }

    @Override
    public void delete(Integer integer) throws SQLException {

    }

    private Sala mapear(ResultSet rs,Sala sala)throws SQLException{
        sala.setId(rs.getInt("id"));
        int sedeId = rs.getInt("id_sede");
        if (rs.wasNull()) {
            throw new SQLException("La sala no tiene sede_id");
        }
        sala.setSede();
        sala.setTipoSala(rs.getString("tipo_sala"));

        sala.setButacas(rs.wasNull()?null:new ButacaDaoImpl().findByIdSala(rs.getInt("id"));
        sala.setFunciones(rs.wasNull()?null:new FuncionDAOImpl().findByIdSala(rs.getInt("id"));
        sala.setAforo(rs.getInt("aforo"));
        sala.setNombre(rs.getString("nombre"));
    }
}
