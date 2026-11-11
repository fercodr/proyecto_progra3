package pe.edu.pucp.entrefunciones.rest.dto.usuarios;

//Recuperar contraseña: correo, DNI, contraseña nueva
public class RecuperarContrasenaDTO {
    private String correo;
    private String dni;
    private String contrasenaNueva;

    public RecuperarContrasenaDTO() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getContrasenaNueva() {
        return contrasenaNueva;
    }

    public void setContrasenaNueva(String contrasenaNueva) {
        this.contrasenaNueva = contrasenaNueva;
    }
}
