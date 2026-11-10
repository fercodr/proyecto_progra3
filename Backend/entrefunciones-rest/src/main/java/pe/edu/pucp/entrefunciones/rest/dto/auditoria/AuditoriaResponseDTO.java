package pe.edu.pucp.entrefunciones.rest.dto.auditoria;

import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;

public class AuditoriaResponseDTO {

    private int id;
    private FuncionResumenDTO funcion;
    private LocalDateTime fecha;
    private AdministradorResumenDTO responsable;
    private String responsableTextoOriginal;
    private String descripcion;
    private String estado;

    public AuditoriaResponseDTO() {
    }

    public AuditoriaResponseDTO(Auditoria auditoria) {
        this.id = auditoria.getId();
        if (auditoria.getFuncion() != null) {
            this.funcion = new FuncionResumenDTO(auditoria.getFuncion());
        }
        this.fecha = auditoria.getFecha();
        if (auditoria.getResponsable() != null) {
            this.responsable = new AdministradorResumenDTO(
                    auditoria.getResponsable());
        } else {
            this.responsableTextoOriginal =
                    auditoria.getResponsableTextoOriginal();
        }
        this.descripcion = auditoria.getDescripcion();
        this.estado = auditoria.getEstado().name();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FuncionResumenDTO getFuncion() {
        return funcion;
    }

    public void setFuncion(FuncionResumenDTO funcion) {
        this.funcion = funcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public AdministradorResumenDTO getResponsable() {
        return responsable;
    }

    public void setResponsable(AdministradorResumenDTO responsable) {
        this.responsable = responsable;
    }

    public String getResponsableTextoOriginal() {
        return responsableTextoOriginal;
    }

    public void setResponsableTextoOriginal(
            String responsableTextoOriginal) {
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
