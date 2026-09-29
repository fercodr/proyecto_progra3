package pe.edu.pucp.entrefunciones.dao.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

import java.sql.SQLException;


public interface AdministradorDAO extends DAO<Administrador, Integer> {
    Administrador findByCodigoEmpleado(String codigoEmpleado) throws SQLException;
}
