package pe.edu.pucp.entrefunciones.rest.dto.usuarios;

// lo que envía C# para cambiar la contraseña
public class CambiarPasswordDTO {
    private String contrasenaActual;
    private String contrasenaNueva;

    public CambiarPasswordDTO() {
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getContrasenaNueva() {
        return contrasenaNueva;
    }

    public void setContrasenaNueva(String contrasenaNueva) {
        this.contrasenaNueva = contrasenaNueva;
    }
}
