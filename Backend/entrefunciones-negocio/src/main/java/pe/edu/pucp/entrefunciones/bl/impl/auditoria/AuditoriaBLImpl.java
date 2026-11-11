package pe.edu.pucp.entrefunciones.bl.impl.auditoria;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.auditoria.AuditoriaBL;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.auditoria.AuditoriaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.AdministradorDAOImpl;
import pe.edu.pucp.entrefunciones.dao.usuarios.AdministradorDAO;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public class AuditoriaBLImpl implements AuditoriaBL {

    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Lima");
    private final AuditoriaDAO auditoriaDAO =  new AuditoriaDAOImpl();
    private final FuncionDAO funcionDAO = new FuncionDAOImpl();
    private final AdministradorDAO administradorDAO=new AdministradorDAOImpl();
    private final Clock reloj;

    public AuditoriaBLImpl() {
        this( Clock.system(ZONA_NEGOCIO));
    }

    public AuditoriaBLImpl(Clock reloj) {
        if (reloj == null) {
            throw new IllegalArgumentException( "El DAO y el reloj son obligatorios");
        }

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
            Auditoria auditoria = auditoriaDAO.findById(id);
            if (auditoria == null) {
                throw new NoEncontradoException(
                        "No existe una auditoria con id " + id);
            }
            return auditoria;
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la auditoria", e);
        }
    }

    @Override
    public List<Auditoria> findByFilters(String descripcion,EstadoAuditoria estado)throws BLException {
        try {
            return auditoriaDAO.findByFilters(descripcion, estado);
        } catch (SQLException e) {
            throw new BLException("No se pudo filtrar las auditorias", e);
        }
    }

    @Override
    public List<Administrador> findResponsablesActivos()
            throws BLException {
        try {
            return administradorDAO.findAllActiveAdmins();
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar los responsables disponibles", e);
        }
    }

    @Override
    public List<Funcion> findFuncionesAsignables() throws BLException {
        try {
            return funcionDAO.findAllActiveFunctions();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar las funciones disponibles", e);
        }
    }

    @Override
    public Auditoria registrar(Integer funcionId, Integer responsableId,String descripcion) throws BLException {
        validarId(funcionId, "funcion");
        validarId(responsableId, "responsable");
        validarDescripcion(descripcion);

        try {
            Funcion funcion = buscarFuncionAsignable(funcionId);
            Administrador responsable = buscarResponsableActivo(responsableId);
            LocalDateTime fechaServidor = LocalDateTime.ofInstant(reloj.instant(), ZONA_NEGOCIO);
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
            throw new ValidacionException("El estado es obligatorio");
        }

        try {
            Auditoria existente = auditoriaDAO.findById(auditoriaId);
            if (existente == null) {
                throw new NoEncontradoException(
                        "No existe una auditoria con id " + auditoriaId);
            }
            Administrador responsable = buscarResponsableActivo(responsableId);
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
        if (auditoria == null || auditoria.getFuncion() == null|| auditoria.getResponsable() == null) throw new BLException("La auditoria debe indicar funcion y responsable");
        try{
            auditoriaDAO.insert(auditoria);
        }catch(SQLException e){
            throw new BLException("No se pudo insertar la auditoria", e);
        }
    }


    @Override
    public void update(Auditoria auditoria) throws BLException {
        if (auditoria == null || auditoria.getResponsable() == null) throw new BLException("La auditoria y su responsable son obligatorios");
        try{
            auditoriaDAO.update(auditoria);
        }catch(SQLException e){
            throw new BLException("No se pudo insertar la auditoria", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "auditoria");
        throw new BLException(
                "Las auditorias forman parte del historial y no se eliminan");
    }

    private void validarDescripcion(String descripcion) throws BLException {
        if (descripcion == null || descripcion.isBlank()) {
            throw new ValidacionException("La descripcion es obligatoria");
        }
        if (descripcion.trim().length() > 500) {
            throw new ValidacionException(
                    "La descripcion no puede superar 500 caracteres");
        }
    }

    private Funcion buscarFuncionAsignable(Integer funcionId)
            throws BLException, SQLException {
        for (Funcion funcion : funcionDAO.findAllActiveFunctions()) {
            if (funcion.getId() == funcionId) {
                return funcion;
            }
        }
        throw new ValidacionException("La funcion no existe o no es asignable");
    }

    private Administrador buscarResponsableActivo(Integer responsableId)
            throws BLException, SQLException {
        for (Administrador responsable : administradorDAO.findAllActiveAdmins()) {
            if (responsable.getId() == responsableId) {
                return responsable;
            }
        }
        throw new ValidacionException(
                "El responsable no existe o no esta activo");
    }

    private void validarId(Integer id, String entidad) throws BLException {
        if (id == null || id <= 0) {
            throw new ValidacionException(
                    "El id de " + entidad + " debe ser positivo");
        }
    }
}
