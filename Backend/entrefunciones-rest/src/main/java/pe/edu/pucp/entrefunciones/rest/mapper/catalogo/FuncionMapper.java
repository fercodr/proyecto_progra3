package pe.edu.pucp.entrefunciones.rest.mapper.catalogo;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.EventoBL;
import pe.edu.pucp.entrefunciones.bl.impl.eventos.EventoBLImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.FuncionDTO;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Convierte entre Funcion y el FuncionDTO plano usado por el frontend. nombreSala/
 * nombreSede/ciudadSede se "desnormalizan" directamente desde el grafo Sala/Sede que
 * FuncionDAOImpl ya carga completo al leer una funcion (ver SalaDAOImpl.mapear()), así
 * el cliente no necesita hacer una llamada aparte a /sedes o /salas solo para mostrar
 * el nombre de la sala en Cartelera/Detalle.
 */
public final class FuncionMapper {
    private EventoBL eventoBL = new EventoBLImpl();

    private FuncionMapper() {
    }

    public static FuncionDTO toDTO(Funcion funcion) {
        FuncionDTO dto = new FuncionDTO();
        dto.setId(funcion.getId());
        dto.setIdEvento(funcion.getEvento() == null ? 0 : funcion.getEvento().getId());
        dto.setTituloEvento(funcion.getEvento() == null ? null : funcion.getEvento().getTitulo());

        Sala sala = funcion.getSala();
        if (sala != null) {
            dto.setIdSala(sala.getId());
            dto.setNombreSala(sala.getNombre());
            if (sala.getSede() != null) {
                dto.setIdSede(sala.getSede().getId());
                dto.setNombreSede(sala.getSede().getRazonSocial());
                dto.setCiudadSede(sala.getSede().getCiudad());
            }
        }

        dto.setFecha(funcion.getFecha() == null ? null : funcion.getFecha().toString());
        dto.setHoraInicio(funcion.getHoraInicio() == null ? null : funcion.getHoraInicio().toString());
        dto.setHoraFin(funcion.getHoraFin() == null ? null : funcion.getHoraFin().toString());
        dto.setIdioma(funcion.getIdioma());
        dto.setEstado(funcion.getEstado() == null ? null : funcion.getEstado().name());
        return dto;
    }

    public static List<FuncionDTO> toDTOs(List<Funcion> funciones) {
        List<FuncionDTO> dtos = new ArrayList<>();
        for (Funcion funcion : funciones) {
            dtos.add(toDTO(funcion));
        }
        return dtos;
    }

    /**
     * idSala es el único dato de Sala que llega en el payload: se construye una Sala
     * "liviana" con solo el id (el resto de sus datos los tiene la BD, no hace falta
     * mandarlos de vuelta solo para programar una función).
     */
    public static Funcion toModelo(FuncionDTO dto, Evento evento) throws BLException {
        try {
            Sala sala = new Sala();
            sala.setId(dto.getIdSala());

            LocalDate fecha = dto.getFecha() == null ? null : LocalDate.parse(dto.getFecha());
            LocalTime horaInicio = dto.getHoraInicio() == null ? null : LocalTime.parse(dto.getHoraInicio());
            LocalTime horaFin = dto.getHoraFin() == null ? null : LocalTime.parse(dto.getHoraFin());

            Funcion funcion = new Funcion(dto.getId(), evento, sala, fecha, horaInicio, horaFin, dto.getIdioma());

            if (dto.getEstado() != null) {
                funcion.setEstado(EstadoFuncion.valueOf(dto.getEstado()));
            }

            return funcion;
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new ValidacionException(e.getMessage());
        }
    }

    public static Funcion toModelo(FuncionDTO dto) throws ValidacionException {
        try {
            Sala sala = new Sala();
            sala.setId(dto.getIdSala());

            LocalDate fecha = dto.getFecha() == null ? null : LocalDate.parse(dto.getFecha());
            LocalTime horaInicio = dto.getHoraInicio() == null ? null : LocalTime.parse(dto.getHoraInicio());
            LocalTime horaFin = dto.getHoraFin() == null ? null : LocalTime.parse(dto.getHoraFin());

            Funcion funcion = new Funcion(dto.getId(),EventoMapper.toModelo(dto.getEvento()), sala, fecha, horaInicio, horaFin, dto.getIdioma());

            if (dto.getEstado() != null) {
                funcion.setEstado(EstadoFuncion.valueOf(dto.getEstado()));
            }

            return funcion;
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new ValidacionException(e.getMessage());
        } catch (BLException e) {
            throw new RuntimeException(e);
        }
    }
}
