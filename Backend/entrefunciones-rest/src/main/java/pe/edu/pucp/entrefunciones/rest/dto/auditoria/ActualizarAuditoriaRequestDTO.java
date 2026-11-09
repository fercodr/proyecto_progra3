package pe.edu.pucp.entrefunciones.rest.dto.auditoria;

public class ActualizarAuditoriaRequestDTO {

    private Integer responsableId;
    private String estado;

    public ActualizarAuditoriaRequestDTO() {
    }

    public Integer getResponsableId() {
        return responsableId;
    }

    public void setResponsableId(Integer responsableId) {
        this.responsableId = responsableId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
