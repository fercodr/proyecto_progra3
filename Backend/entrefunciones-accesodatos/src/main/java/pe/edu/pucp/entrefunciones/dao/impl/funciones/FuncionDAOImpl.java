package pe.edu.pucp.entrefunciones.dao.impl.funciones;

import pe.edu.pucp.entrefunciones.dao.funciones.FuncionDAO;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.sql.SQLException;
import java.util.List;

public class FuncionDAOImpl implements FuncionDAO {
    @Override
    public List<Funcion> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public Funcion findById(Integer integer) throws SQLException {
        return null;
    }

    @Override
    public void insert(Funcion modelo) throws SQLException {

    }

    @Override
    public void update(Funcion modelo) throws SQLException {

    }

    @Override
    public void delete(Integer integer) throws SQLException {

    }

    @Override
    public List<Funcion> findByIdSala(int id) {
        return List.of();
    }
}
