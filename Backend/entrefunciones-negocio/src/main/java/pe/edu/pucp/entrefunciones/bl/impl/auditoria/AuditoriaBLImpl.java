package pe.edu.pucp.entrefunciones.bl.impl.auditoria;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.auditoria.AuditoriaBL;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.auditoria.AuditoriaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public class AuditoriaBLImpl implements AuditoriaBL {

    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Lima");
    private final AuditoriaDAO auditoriaDAO;
    private final Clock reloj;

    public AuditoriaBLImpl() {
        this(new AuditoriaDAOImpl(), Clock.system(ZONA_NEGOCIO));
    }

    public AuditoriaBLImpl(AuditoriaDAO auditoriaDAO, Clock reloj) {
        if (auditoriaDAO == null || reloj == null) {
            throw new IllegalArgumentException(
                    "El DAO y el reloj son obligatorios");
        }
        this.auditoriaDAO = auditoriaDAO;
        this.reloj = reloj;
    }

    @Override
    public List<Auditoria> findAll() throws BLException {
        try {
            return auditoriaDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las auditorias", e);
        }
    }

    @Override
    public Auditoria findById(Integer id) throws BLException {
        validarId(id, "auditoria");
        try {
            return auditoriaDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la auditoria", e);
        }
    }

    @Override
    public List<Auditoria> findByFilters(String descripcion,
                                         EstadoAuditoria estado)
            throws BLException {
        try {
            return auditoriaDAO.findByFilters(descripcion, estado);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo filtrar las auditorias", e);
        }
    }

    @Override
    public List<Administrador> findResponsablesActivos()
            throws BLException {
        try {
            return auditoriaDAO.findResponsablesActivos();
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar los responsables disponibles", e);
        }
    }

    @Override
    public List<Funcion> findFuncionesAsignables() throws BLException {
        try {
            return auditoriaDAO.findFuncionesAsignables();
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar las funciones disponibles", e);
        }
    }

    @Override
    public Auditoria registrar(Integer funcionId, Integer responsableId,
                               String descripcion) throws BLException {
        validarId(funcionId, "funcion");
        validarId(responsableId, "responsable");
        validarDescripcion(descripcion);

        try {
            Funcion funcion = obtenerFuncionAsignable(funcionId);
            Administrador responsable = obtenerResponsableActivo(
                    responsableId);
            LocalDateTime fechaServidor = LocalDateTime.ofInstant(
                    reloj.instant(), ZONA_NEGOCIO);
            Auditoria auditoria = new Auditoria(
                    0,
                    fechaServidor,
                    responsable,
                    descripcion.trim(),
                    funcion);
            auditoria.setEstado(EstadoAuditoria.PENDIENTE);
            auditoriaDAO.insert(auditoria);
            return auditoria;
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar la auditoria", e);
        }
    }

    @Override
    public Auditoria actualizarAtencion(Integer auditoriaId,
                                        Integer responsableId,
                                        EstadoAuditoria estado)
            throws BLException {
        validarId(auditoriaId, "auditoria");
        validarId(responsableId, "responsable");
        if (estado == null) {
            throw new BLException("El estado es obligatorio");
        }

        try {
            Auditoria existente = auditoriaDAO.findById(auditoriaId);
            if (existente == null) {
                throw new BLException(
                        "No existe una auditoria con id " + auditoriaId);
            }
            Administrador responsable = obtenerResponsableActivo(
                    responsableId);

            existente.setResponsable(responsable);
            existente.setEstado(estado);
            auditoriaDAO.update(existente);
            return auditoriaDAO.findById(auditoriaId);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar la auditoria", e);
        }
    }

    @Override
    public void insert(Auditoria auditoria) throws BLException {
        if (auditoria == null || auditoria.getFuncion() == null
                || auditoria.getResponsable() == null) {
            throw new BLException(
                    "La auditoria debe indicar funcion y responsable");
        }
        Auditoria registrada = registrar(
                auditoria.getFuncion().getId(),
                auditoria.getResponsable().getId(),
                auditoria.getDescripcion());
        auditoria.setId(registrada.getId());
        auditoria.setFecha(registrada.getFecha());
        auditoria.setFuncion(registrada.getFuncion());
        auditoria.setResponsable(registrada.getResponsable());
        auditoria.setEstado(registrada.getEstado());
    }

    @Override
    public void update(Auditoria auditoria) throws BLException {
        if (auditoria == null || auditoria.getResponsable() == null) {
            throw new BLException(
                    "La auditoria y su responsable son obligatorios");
        }
        actualizarAtencion(
                auditoria.getId(),
                auditoria.getResponsable().getId(),
                auditoria.getEstado());
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "auditoria");
        throw new BLException(
                "Las auditorias forman parte del historial y no se eliminan");
    }

    private Administrador obtenerResponsableActivo(Integer responsableId)
            throws SQLException, BLException {
        Administrador responsable = auditoriaDAO.findResponsableById(
                responsableId);
        if (responsable == null || !responsable.estaActivo()) {
            throw new BLException(
                    "El responsable debe existir y ser un administrador activo");
        }
        return responsable;
    }

    private Funcion obtenerFuncionAsignable(Integer funcionId)
            throws SQLException, BLException {
        Funcion funcion = auditoriaDAO.findFuncionById(funcionId);
        if (funcion == null) {
            throw new BLException("La funcion indicada no existe");
        }
        if (funcion.getEstado() == EstadoFuncion.CANCELADA
                || !funcion.getEvento().estaActivo()
                || !funcion.getSala().estaActivo()
                || !funcion.getSala().getSede().estaActivo()) {
            throw new BLException(
                    "La funcion no esta disponible para una incidencia nueva");
        }
        return funcion;
    }

    private void validarDescripcion(String descripcion) throws BLException {
        if (descripcion == null || descripcion.isBlank()) {
            throw new BLException("La descripcion es obligatoria");
        }
        if (descripcion.trim().length() > 500) {
            throw new BLException(
                    "La descripcion no puede superar 500 caracteres");
        }
    }

    private void validarId(Integer id, String entidad) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException(
                    "El id de " + entidad + " debe ser positivo");
        }
    }
}
