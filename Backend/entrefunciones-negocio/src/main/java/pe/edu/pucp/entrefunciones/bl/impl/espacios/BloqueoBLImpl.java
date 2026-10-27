package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.espacios.BloqueoBL;
import pe.edu.pucp.entrefunciones.dao.espacios.BloqueoDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.RecursoDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.BloqueoDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.RecursoDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

import java.sql.SQLException;
import java.util.List;

public class BloqueoBLImpl implements BloqueoBL {
    private final BloqueoDAO bloqueoDAO;
    private final SalaDAO salaDAO;
    private final RecursoDAO recursoDAO;

    public BloqueoBLImpl() {
        this(new BloqueoDAOImpl(), new SalaDAOImpl(), new RecursoDAOImpl());
    }

    public BloqueoBLImpl(BloqueoDAO bloqueoDAO, SalaDAO salaDAO, RecursoDAO recursoDAO) {
        if (bloqueoDAO == null || salaDAO == null || recursoDAO == null) {
            throw new IllegalArgumentException("Los DAO de bloqueo no pueden ser nulos");
        }
        this.bloqueoDAO = bloqueoDAO;
        this.salaDAO = salaDAO;
        this.recursoDAO = recursoDAO;
    }

    @Override
    public List<Bloqueo> findAll() throws BLException {
        try {
            return bloqueoDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar los bloqueos", e);
        }
    }

    @Override
    public List<Bloqueo> listarPorSala(int idSala) throws BLException {
        validarId(idSala, "sala");
        validarExisteSala(idSala);
        try {
            return bloqueoDAO.findBySalaId(idSala);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar los bloqueos de la sala", e);
        }
    }

    @Override
    public List<Bloqueo> listarPorRecurso(int idRecurso) throws BLException {
        validarId(idRecurso, "recurso");
        validarExisteRecurso(idRecurso);
        try {
            return bloqueoDAO.findByRecursoId(idRecurso);
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar los bloqueos del recurso", e);
        }
    }

    @Override
    public Bloqueo findById(Integer id) throws BLException {
        validarId(id, "bloqueo");
        try {
            return bloqueoDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el bloqueo", e);
        }
    }

    @Override
    public void insert(Bloqueo bloqueo) throws BLException {
        validarDatos(bloqueo);
        TransaccionBL.ejecutar("No se pudo registrar el bloqueo", () -> {
            validarObjetivo(bloqueo);
            validarSolapamiento(bloqueo, null);
            bloqueoDAO.insert(bloqueo);
        });
    }

    @Override
    public void update(Bloqueo bloqueo) throws BLException {
        validarDatos(bloqueo);
        validarId(bloqueo.getId(), "bloqueo");
        TransaccionBL.ejecutar("No se pudo actualizar el bloqueo", () -> {
            validarExisteBloqueo(bloqueo.getId());
            validarObjetivo(bloqueo);
            validarSolapamiento(bloqueo, bloqueo.getId());
            bloqueoDAO.update(bloqueo);
        });
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "bloqueo");
        TransaccionBL.ejecutar("No se pudo cancelar el bloqueo", () -> {
            validarExisteBloqueo(id);
            throw new BLException(
                    "No se puede cancelar el bloqueo porque el esquema actual no admite baja lógica");
        });
    }

    private Bloqueo validarExisteBloqueo(Integer id) throws BLException {
        try {
            Bloqueo bloqueo = bloqueoDAO.findById(id);
            if (bloqueo == null) {
                throw new BLException("No existe un bloqueo con id " + id);
            }
            return bloqueo;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del bloqueo", e);
        }
    }

    private Sala validarExisteSala(Integer id) throws BLException {
        try {
            Sala sala = salaDAO.findById(id);
            if (sala == null) {
                throw new BLException("No existe la sala indicada");
            }
            return sala;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la sala del bloqueo", e);
        }
    }

    private Recurso validarExisteRecurso(Integer id) throws BLException {
        try {
            Recurso recurso = recursoDAO.findById(id);
            if (recurso == null) {
                throw new BLException("No existe el recurso indicado");
            }
            return recurso;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el recurso del bloqueo", e);
        }
    }

    private void validarObjetivo(Bloqueo bloqueo) throws BLException {
        if (bloqueo.getSala() != null) {
            validarId(bloqueo.getSala().getId(), "sala");
            Sala sala = validarExisteSala(bloqueo.getSala().getId());
            if (!sala.estaActivo()) {
                throw new BLException("No se puede bloquear una sala inactiva");
            }
        } else {
            validarId(bloqueo.getRecurso().getId(), "recurso");
            Recurso recurso = validarExisteRecurso(bloqueo.getRecurso().getId());
            if (!recurso.estaActivo()) {
                throw new BLException("No se puede bloquear un recurso inactivo");
            }
        }
    }

    private void validarSolapamiento(Bloqueo bloqueo, Integer idExcluir) throws BLException {
        try {
            boolean existeCruce;
            if (bloqueo.getSala() != null) {
                existeCruce = bloqueoDAO.existsOverlapBySalaId(
                        bloqueo.getSala().getId(), bloqueo.getFechaInicio(),
                        bloqueo.getFechaFin(), idExcluir);
            } else {
                existeCruce = bloqueoDAO.existsOverlapByRecursoId(
                        bloqueo.getRecurso().getId(), bloqueo.getFechaInicio(),
                        bloqueo.getFechaFin(), idExcluir);
            }
            if (existeCruce) {
                throw new BLException(
                        "El período se superpone con otro bloqueo del mismo objetivo");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el solapamiento del bloqueo", e);
        }
    }

    private void validarDatos(Bloqueo bloqueo) throws BLException {
        if (bloqueo == null) {
            throw new BLException("El bloqueo no puede ser nulo");
        }
        if (bloqueo.getMotivo() == null || bloqueo.getMotivo().trim().isEmpty()) {
            throw new BLException("El motivo del bloqueo no puede estar vacío");
        }
        if (bloqueo.getMotivo().length() > 200) {
            throw new BLException("El motivo del bloqueo no puede exceder 200 caracteres");
        }
        if ((bloqueo.getSala() == null) == (bloqueo.getRecurso() == null)) {
            throw new BLException(
                    "El bloqueo debe indicar una sala o un recurso, exclusivamente");
        }
        if (bloqueo.getFechaInicio() == null || bloqueo.getFechaFin() == null) {
            throw new BLException("Las fechas del bloqueo no pueden ser nulas");
        }
        if (!bloqueo.getFechaFin().isAfter(bloqueo.getFechaInicio())) {
            throw new BLException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
    }

    private void validarId(Integer id, String entidad) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de " + entidad + " debe ser positivo");
        }
    }
}
