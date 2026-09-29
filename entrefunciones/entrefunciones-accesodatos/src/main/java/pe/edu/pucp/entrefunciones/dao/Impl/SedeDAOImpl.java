package pe.edu.pucp.entrefunciones.dao.Impl;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.dao.SedeDAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.SQLException;
import java.util.List;

public class SedeDAOImpl implements SedeDAO {

    @Override
    public List<Sede> findAll() throws SQLException {
        return List.of();
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
}
