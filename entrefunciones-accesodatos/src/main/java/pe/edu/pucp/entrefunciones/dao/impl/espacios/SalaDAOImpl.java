package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

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
        int sedeId = rs.getInt("sede_id");
        if (rs.wasNull()) {
            throw new SQLException("La sala no tiene sede_id");
        }
        sala.setSede(new SedeDAOImpl().findById(sedeId));
        sala.setTipoSala(rs.getString("tipo_sala"));
        List <Butaca> butacas = new ButacaDAOImpl().findBySalaId(rs.getInt("id"));
        sala.setButacas(rs.wasNull()?null:butacas);
        sala.setFunciones(rs.wasNull()?null:  new  FuncionDAOImpl().findBySalaId(rs.getInt("id"));
        sala.setAforo(rs.getInt("aforo"));
        sala.setNombre(rs.getString("nombre"));
    }
}
