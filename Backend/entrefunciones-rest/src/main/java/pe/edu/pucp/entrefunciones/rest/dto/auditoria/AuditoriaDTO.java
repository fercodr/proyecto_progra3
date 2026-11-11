package pe.edu.pucp.entrefunciones.rest.dto.auditoria;

import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.rest.dto.catalogo.FuncionDTO;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.AdministradorDTO;

public class AuditoriaDTO {

    private int id;
    private FuncionDTO funcion;
    private LocalDateTime fecha;
    private AdministradorDTO responsable;
    private String responsableTextoOriginal;
    private String descripcion;
    private String estado;
    public AuditoriaDTO() {
    }

    public AuditoriaDTO(Auditoria auditoria) {
        this.id = auditoria.getId();
        this.funcion = auditoria.getFuncion() == null
                ? null : pe.edu.pucp.entrefunciones.rest.mapper.catalogo.FuncionMapper
                .toDTO(auditoria.getFuncion());
        this.fecha = auditoria.getFecha();
        if (auditoria.getResponsable() != null) {
            this.responsable = new AdministradorDTO(auditoria.getResponsable());
        }
        this.descripcion = auditoria.getDescripcion();
        this.responsableTextoOriginal = auditoria.getResponsableTextoOriginal();
        this.estado = auditoria.getEstado().name();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FuncionDTO getFuncion() {
        return funcion;
    }

    public void setFuncion(FuncionDTO funcion) {
        this.funcion = funcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public AdministradorDTO getResponsable() {
        return responsable;
    }

    public void setResponsable(AdministradorDTO responsable) {
        this.responsable = responsable;
    }

    public String getResponsableTextoOriginal() {
        return responsableTextoOriginal;
    }

    public void setResponsableTextoOriginal(String responsableTextoOriginal) {
        this.responsableTextoOriginal = responsableTextoOriginal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
