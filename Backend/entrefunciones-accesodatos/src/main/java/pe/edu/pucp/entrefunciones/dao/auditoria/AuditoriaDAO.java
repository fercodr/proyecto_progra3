package pe.edu.pucp.entrefunciones.dao.auditoria;

import java.sql.SQLException;
import java.util.List;
import pe.edu.pucp.entrefunciones.dao.DAO;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public interface AuditoriaDAO extends DAO<Auditoria, Integer> {
    public List<Auditoria> findByFilters(String descripcion, EstadoAuditoria estado) throws SQLException;
}
