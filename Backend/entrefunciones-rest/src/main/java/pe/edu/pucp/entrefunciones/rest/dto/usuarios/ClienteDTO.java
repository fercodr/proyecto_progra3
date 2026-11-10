package pe.edu.pucp.entrefunciones.rest.dto.usuarios;
//ClienteDTO para guardar los datos personales de los clientes
//Se creo un DTO porque Glassfish necesita un constructor vacío para pasar el objeto a Java, pero en la capa modelos no hay constructor vacio. El registro fallaria
// El getter se llama estaActivo(). GlassFish no lo reconoce como propiedad, así que el JSON saldría sin activo.
//Cliente tiene getEntradas(), y podrian haber problemas.
public class ClienteDTO {
    private int id;
    private boolean activo;
    private String nombres;
    private String apellidos;
    private String correo;
    private String contrasena;
    private String dni;
    private String telefono;
    private String fechaRegistro;

    public ClienteDTO() {
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
