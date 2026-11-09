package pe.edu.pucp.entrefunciones.rest.dto.auditoria;

public class CrearAuditoriaRequestDTO {

    private Integer funcionId;
    private Integer responsableId;
    private String descripcion;

    public CrearAuditoriaRequestDTO() {
    }

    public Integer getFuncionId() {
        return funcionId;
    }

    public void setFuncionId(Integer funcionId) {
        this.funcionId = funcionId;
    }

    public Integer getResponsableId() {
        return responsableId;
    }

    public void setResponsableId(Integer responsableId) {
        this.responsableId = responsableId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
