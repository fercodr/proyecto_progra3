package pe.edu.pucp.entrefunciones.bl;

import pe.edu.pucp.entrefunciones.bl.error.BLException;

import java.sql.SQLException;
import java.util.List;

public interface BL<T, ID> {

    List<T> findAll() throws BLException;

    T findById(ID id) throws BLException;

    void insert(T entidad) throws BLException;

    void update(T entidad) throws BLException;

    void delete(ID id) throws BLException;
}
