package pe.edu.pucp.entrefunciones.rest.mapper.ventas;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago;
import pe.edu.pucp.entrefunciones.rest.dto.ventas.PagoDTO;

import java.util.ArrayList;
import java.util.List;

public final class PagoMapper {

    private PagoMapper() {
    }

    public static PagoDTO toDTO(Pago pago) {
        if (pago == null) {
            return null;
        }
        PagoDTO dto = new PagoDTO();
        dto.setId(pago.getId());
        dto.setCodigoPago(pago.getCodigoPago());
        dto.setMonto(pago.getMonto());
        dto.setFechaPago(pago.getFechaPago());
        dto.setEstado(pago.getEstado() == null ? null : pago.getEstado().name());

        if (pago.getReserva() != null) {
            dto.setReservaId(pago.getReserva().getId());
            if (pago.getReserva().getCliente() != null) {
                dto.setCliente((pago.getReserva().getCliente().getNombres() + " "
                        + pago.getReserva().getCliente().getApellidos()).trim());
            }
        }

        return dto;
    }

    public static List<PagoDTO> toDTOs(List<Pago> pagos) {
        List<PagoDTO> dtos = new ArrayList<>();
        if (pagos != null) {
            for (Pago pago : pagos) {
                dtos.add(toDTO(pago));
            }
        }
        return dtos;
    }

    public static Pago toModelo(PagoDTO dto) throws BLException {
        if (dto == null) {
            return null;
        }
        try {
            EstadoPago estadoEnum = dto.getEstado() == null ? null : EstadoPago.valueOf(dto.getEstado());
            return null;
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage());
        }
    }
}
