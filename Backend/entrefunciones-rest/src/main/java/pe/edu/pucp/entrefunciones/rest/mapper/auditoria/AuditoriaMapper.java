package pe.edu.pucp.entrefunciones.rest.mapper.auditoria;

import java.util.ArrayList;
import java.util.List;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.rest.dto.auditoria.AuditoriaDTO;
import pe.edu.pucp.entrefunciones.rest.mapper.catalogo.FuncionMapper;
import pe.edu.pucp.entrefunciones.rest.mapper.usuarios.AdministradorMapper;

public final class AuditoriaMapper {

    private AuditoriaMapper() {}

    public static AuditoriaDTO toDTO(Auditoria auditoria) {
        if (auditoria == null) return null;
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setId(auditoria.getId());
        dto.setFuncion(auditoria.getFuncion() == null
                ? null : FuncionMapper.toDTO(auditoria.getFuncion()));
        dto.setFecha(auditoria.getFecha());
        dto.setResponsable(AdministradorMapper.toDTO(auditoria.getResponsable()));
        dto.setResponsableTextoOriginal(auditoria.getResponsableTextoOriginal());
        dto.setDescripcion(auditoria.getDescripcion());
        dto.setEstado(auditoria.getEstado().name());
        return dto;
    }

    public static List<AuditoriaDTO> toDTOs(List<Auditoria> auditorias) {
        List<AuditoriaDTO> dtos = new ArrayList<>();
        if (auditorias == null)return dtos;
        for (Auditoria auditoria : auditorias)if (auditoria != null) dtos.add(toDTO(auditoria));
        return dtos;
    }
    public static Auditoria toModel(AuditoriaDTO dto) throws BLException {
        if (dto == null) return null;
        Auditoria auditoria = new Auditoria();
        auditoria.setId(dto.getId());
        auditoria.setFuncion(FuncionMapper.toModelo(dto.getFuncion()));
        auditoria.setFecha(dto.getFecha());
        auditoria.setResponsable(AdministradorMapper.toModelo(dto.getResponsable()));
        auditoria.setDescripcion(dto.getDescripcion());
        auditoria.setEstado(EstadoAuditoria.valueOf(dto.getEstado()));
        return auditoria;
    }
    public static List<Auditoria> toModels(List<AuditoriaDTO> dtos) throws BLException {
        List<Auditoria> auditorias = new ArrayList<>();
        if (dtos == null) return auditorias;
        for (AuditoriaDTO dto : dtos) {
            auditorias.add(toModel(dto));
        }
        return auditorias;
    }
}
