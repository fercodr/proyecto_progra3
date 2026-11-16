package pe.edu.pucp.entrefunciones.dao.impl.eventos;


import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
public interface FuncionDAO extends DAO<Funcion, Integer> {
    public List<Funcion> findAllActiveFunctions() throws SQLException ;
    void insertFunciones(int idEvento, List<Funcion> funciones) throws SQLException;

    void updateFunciones(int idEvento, List<Funcion> funciones) throws SQLException;

    void deleteFunciones(int idEvento) throws SQLException;

    List<Funcion> findByEvento(Evento evento) throws SQLException;

    List<Funcion> findBySalaId(int idSala) throws SQLException;

    List<Funcion> findReportes(LocalDate desde, LocalDate hasta,
                               Integer idSede, Integer idSala,
                               String tipoEvento)
            throws SQLException;
}
