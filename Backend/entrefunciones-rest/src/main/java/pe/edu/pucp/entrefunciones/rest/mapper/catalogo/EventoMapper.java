package pe.edu.pucp.entrefunciones.rest.mapper.catalogo;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.EventoDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte entre Evento (y sus subtipos Pelicula/ObraTeatro/ActividadCultural) y el
 * EventoDTO plano usado por el frontend. El campo "tipo" del DTO es el discriminador:
 * "PELICULA" | "OBRA_TEATRO" | "ACTIVIDAD_CULTURAL" (mismos valores que usa
 * EventoDAOImpl.construir() para la columna tipo_evento).
 */
public final class EventoMapper {

    private EventoMapper() {
    }

    public static EventoDTO toDTO(Evento evento) {
        EventoDTO dto = new EventoDTO();
        dto.setId(evento.getId());
        dto.setActivo(evento.estaActivo());
        dto.setPrecio(evento.getPrecio());
        dto.setTitulo(evento.getTitulo());
        dto.setImagenUrl(evento.getImagenUrl());
        dto.setRequiereAsientoNumerado(evento.requiereAsientoNumerado());

        if (evento instanceof Pelicula pelicula) {
            dto.setTipo("PELICULA");
            dto.setDuracionMinutos(pelicula.getDuracionMinutos());
            dto.setGenero(pelicula.getGenero() == null ? null : pelicula.getGenero().name());
            dto.setClasificacionPorEdad(pelicula.getClasificacionPorEdad() == null
                    ? null : pelicula.getClasificacionPorEdad().name());
            dto.setDirector(pelicula.getDirector());
        } else if (evento instanceof ObraTeatro obra) {
            dto.setTipo("OBRA_TEATRO");
            dto.setNombreDirector(obra.getNombreDirector());
            dto.setElenco(obra.getElenco());
        } else if (evento instanceof ActividadCultural actividad) {
            dto.setTipo("ACTIVIDAD_CULTURAL");
            dto.setExpositor(actividad.getExpositor());
            dto.setTipoActividad(actividad.getTipoActividad() == null
                    ? null : actividad.getTipoActividad().name());
        }

        return dto;
    }

    /** Igual que toDTO pero incluye las funciones del evento (usar con ?completo=true). */
    public static EventoDTO toDTOCompleto(Evento evento) {
        EventoDTO dto = toDTO(evento);
        dto.setFunciones(FuncionMapper.toDTOs(evento.getFunciones()));
        return dto;
    }

    public static List<EventoDTO> toDTOs(List<Evento> eventos) {
        List<EventoDTO> dtos = new ArrayList<>();
        for (Evento evento : eventos) {
            dtos.add(toDTO(evento));
        }
        return dtos;
    }

    public static Evento toModelo(EventoDTO dto) throws BLException {
        if (dto.getTipo() == null) {
            throw new ValidacionException("El evento debe indicar su tipo");
        }

        Evento evento;
        try {
            switch (dto.getTipo()) {
                case "PELICULA" -> evento = new Pelicula(
                        dto.getId(), dto.getPrecio(), dto.getTitulo(),
                        dto.getDuracionMinutos() == null ? 0 : dto.getDuracionMinutos(),
                        dto.getGenero() == null ? null : CategoriaGenero.valueOf(dto.getGenero()),
                        dto.getClasificacionPorEdad() == null ? null : ClasificacionEdad.valueOf(dto.getClasificacionPorEdad()),
                        dto.getDirector());
                case "OBRA_TEATRO" -> evento = new ObraTeatro(
                        dto.getId(), dto.getPrecio(), dto.getTitulo(),
                        dto.getNombreDirector(), dto.getElenco());
                case "ACTIVIDAD_CULTURAL" -> evento = new ActividadCultural(
                        dto.getId(), dto.getPrecio(), dto.getTitulo(),
                        dto.getExpositor(),
                        dto.getTipoActividad() == null ? null : TipoActividad.valueOf(dto.getTipoActividad()));
                default -> throw new ValidacionException("Tipo de evento no reconocido: " + dto.getTipo());
            }

            evento.setImagenUrl(dto.getImagenUrl());

            if (!dto.isActivo()) {
                evento.desactivar();
            }

            if (dto.getFunciones() != null) {
                for (var funcionDTO : dto.getFunciones()) {
                    Funcion funcion = FuncionMapper.toModelo(funcionDTO, evento);
                    evento.agregarFuncion(funcion);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }

        return evento;
    }
}
