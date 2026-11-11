package pe.edu.pucp.entrefunciones.rest.dto.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public class AdministradorDTO {
    private int id;
    private boolean activo;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private String dni;
    private String codigoEmpleado;

    public AdministradorDTO() { }

    public AdministradorDTO(Administrador administrador) {
        if (administrador != null) {
            id = administrador.getId();
            activo = administrador.estaActivo();
            nombres = administrador.getNombres();
            apellidos = administrador.getApellidos();
            correo = administrador.getCorreo();
            dni = administrador.getDni();
            codigoEmpleado = administrador.getCodigoEmpleado();
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getCodigoEmpleado() { return codigoEmpleado; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }
}
