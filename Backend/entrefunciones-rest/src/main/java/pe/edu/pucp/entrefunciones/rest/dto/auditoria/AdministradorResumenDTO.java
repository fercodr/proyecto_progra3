package pe.edu.pucp.entrefunciones.rest.dto.auditoria;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public class AdministradorResumenDTO {

    private int id;
    private String nombres;
    private String apellidos;
    private boolean activo;

    public AdministradorResumenDTO() {
    }

    public AdministradorResumenDTO(Administrador administrador) {
        this.id = administrador.getId();
        this.nombres = administrador.getNombres();
        this.apellidos = administrador.getApellidos();
        this.activo = administrador.estaActivo();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
