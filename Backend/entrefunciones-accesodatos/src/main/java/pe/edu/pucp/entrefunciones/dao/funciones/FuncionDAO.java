package pe.edu.pucp.entrefunciones.dao.funciones;

import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.util.List;

public interface FuncionDAO extends DAO<Funcion,Integer> {
    List<Funcion> findByIdSala(int id);
}
