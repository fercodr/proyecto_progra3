package pe.edu.pucp.entrefunciones.rest.mapper.ventas;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoEntrada;
import pe.edu.pucp.entrefunciones.rest.dto.ventas.EntradaDTO;

import java.util.ArrayList;
import java.util.List;

public final class EntradaMapper {

    private EntradaMapper() {
    }

    public static EntradaDTO toDTO(Entrada entrada) {
        if (entrada == null) {
            return null;
        }
        EntradaDTO dto = new EntradaDTO();
        dto.setId(entrada.getId());
        dto.setFechaEmision(entrada.getFechaEmision());
        dto.setCodigoQR(entrada.getCodigoQR());
        dto.setPrecioFinal(entrada.getPrecioFinal());
        dto.setEstado(entrada.getEstado() == null ? null : entrada.getEstado().name());

        if (entrada.getReserva() != null) {
            dto.setReservaId(entrada.getReserva().getId());
        }

        if (entrada.getButaca() != null) {
            dto.setButacaIdentificador(entrada.getButaca().getFila() + entrada.getButaca().getColumna());
        }

        return dto;
    }

    public static List<EntradaDTO> toDTOs(List<Entrada> entradas) {
        List<EntradaDTO> dtos = new ArrayList<>();
        if (entradas != null) {
            for (Entrada entrada : entradas) {
                dtos.add(toDTO(entrada));
            }
        }
        return dtos;
    }

    public static Entrada toModelo(EntradaDTO dto) throws BLException {
        if (dto == null) {
            return null;
        }
        try {
            EstadoEntrada estadoEnum = dto.getEstado() == null ? null : EstadoEntrada.valueOf(dto.getEstado());
            return null;
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
    }
}
