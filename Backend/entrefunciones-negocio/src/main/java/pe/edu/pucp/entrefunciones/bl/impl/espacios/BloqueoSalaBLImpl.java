package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.Transacciones;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.BloqueoSalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.BloqueoSala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;

public class BloqueoSalaBLImpl {
    public record Disponibilidad(boolean disponible, String motivo) { }

    private final BloqueoSalaDAOImpl bloqueos = new BloqueoSalaDAOImpl();
    private final SalaDAOImpl salas = new SalaDAOImpl();
    private final FuncionDAOImpl funciones = new FuncionDAOImpl();
    private final ControlOperacionesDAO control = new ControlOperacionesDAO();

    public List<BloqueoSala> findAll() throws BLException {
        try { return bloqueos.findAll(); }
        catch (SQLException ex) { throw new BLException("No se pudieron listar los bloqueos", ex); }
    }

    public BloqueoSala findById(int id) throws BLException {
        try {
            BloqueoSala bloqueo = bloqueos.findById(id);
            if (bloqueo == null) throw new NoEncontradoException("No existe el bloqueo indicado");
            return bloqueo;
        } catch (SQLException ex) { throw new BLException("No se pudo consultar el bloqueo", ex); }
    }

    public BloqueoSala insert(BloqueoSala bloqueo) throws BLException {
        validar(bloqueo);
        return Transacciones.ejecutar("No se pudo registrar el bloqueo", () -> {
            control.bloquearSala(bloqueo.getSala().getId());
            validarSalaActiva(bloqueo.getSala().getId());
            bloqueos.insert(bloqueo);
            return bloqueo;
        });
    }

    public BloqueoSala update(BloqueoSala bloqueo) throws BLException {
        validar(bloqueo);
        return Transacciones.ejecutar("No se pudo actualizar el bloqueo", () -> {
            if (bloqueos.findById(bloqueo.getId()) == null)
                throw new NoEncontradoException("No existe el bloqueo indicado");
            control.bloquearSala(bloqueo.getSala().getId());
            if (bloqueo.estaActivo()) validarSalaActiva(bloqueo.getSala().getId());
            bloqueos.update(bloqueo);
            return bloqueo;
        });
    }

    public void delete(int id) throws BLException {
        BloqueoSala bloqueo = findById(id);
        bloqueo.desactivar();
        update(bloqueo);
    }

    public Disponibilidad consultar(int salaId, LocalDate fecha, LocalTime inicio,
                                    LocalTime fin, Integer excluirFuncionId)
            throws BLException {
        if (salaId <= 0 || fecha == null || inicio == null || fin == null || !fin.isAfter(inicio))
            throw new ValidacionException("Indique sala, fecha y un horario valido");
        try {
            Sala sala = salas.findById(salaId);
            if (sala == null) return new Disponibilidad(false, "La sala no existe");
            if (!sala.estaActivo() || !sala.getSede().estaActivo())
                return new Disponibilidad(false, "La sala o su sede esta inactiva");
            LocalDateTime desde = fecha.atTime(inicio);
            LocalDateTime hasta = fecha.atTime(fin);
            if (bloqueos.existeSolapamiento(salaId, desde, hasta, null))
                return new Disponibilidad(false, "El horario coincide con un bloqueo vigente de la sala");
            for (var funcion : funciones.findBySalaId(salaId)) {
                if (funcion.getEstado() == EstadoFuncion.CONFIRMADA
                        && (excluirFuncionId == null || funcion.getId() != excluirFuncionId)
                        && funcion.getFecha().equals(fecha)
                        && inicio.isBefore(funcion.getHoraFin())
                        && fin.isAfter(funcion.getHoraInicio()))
                    return new Disponibilidad(false, "El horario coincide con otra funcion confirmada");
            }
            return new Disponibilidad(true, "Horario disponible");
        } catch (SQLException ex) {
            throw new BLException("No se pudo consultar la disponibilidad", ex);
        }
    }

    private void validar(BloqueoSala bloqueo) throws ValidacionException {
        if (bloqueo == null || bloqueo.getSala() == null)
            throw new ValidacionException("El bloqueo y su sala son obligatorios");
        if (!bloqueo.getFin().isAfter(bloqueo.getInicio()))
            throw new ValidacionException("El fin debe ser posterior al inicio");
    }

    private void validarSalaActiva(int salaId) throws SQLException, BLException {
        Sala sala = salas.findById(salaId);
        if (sala == null || !sala.estaActivo() || !sala.getSede().estaActivo())
            throw new ValidacionException("La sala o su sede no esta activa");
    }
}
