package pe.edu.pucp.entrefunciones.rest.dto.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

//AdministradorDTO para guardar los datos personales de los administradores en la base de datos.
//Se creo un DTO porque Glassfish necesita un constructor vacío para pasar el objeto a Java, pero en la capa modelos no hay constructor vacio. El registro fallaria
// El getter se llama estaActivo(). GlassFish no lo reconoce como propiedad, así que el JSON saldría sin activo.
public class AdministradorDTO {
    private int id;
    private boolean activo;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private String dni;
    private String codigoEmpleado;

    public AdministradorDTO() {
    }
    public AdministradorDTO(Administrador administrador) {
        if (administrador != null) {
            this.id = administrador.getId();
            this.activo = administrador.estaActivo();
            this.nombres = administrador.getNombres();
            this.apellidos = administrador.getApellidos();
            this.correo = administrador.getCorreo();
            this.dni = administrador.getDni();
            this.codigoEmpleado = administrador.getCodigoEmpleado();
        }
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }
}
