package pe.edu.pucp.entrefunciones.bl.auditoria;

import java.util.List;
import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public interface AuditoriaBL extends BL<Auditoria, Integer> {

    List<Auditoria> findByFilters(String descripcion, EstadoAuditoria estado)
            throws BLException;

    List<Administrador> findResponsablesActivos() throws BLException;

    List<Funcion> findFuncionesAsignables() throws BLException;

    Auditoria registrar(Integer funcionId, Integer responsableId,
                        String descripcion) throws BLException;

    Auditoria actualizarAtencion(Integer auditoriaId,
                                 Integer responsableId,
                                 String descripcion,
                                 EstadoAuditoria estado)
            throws BLException;
}
