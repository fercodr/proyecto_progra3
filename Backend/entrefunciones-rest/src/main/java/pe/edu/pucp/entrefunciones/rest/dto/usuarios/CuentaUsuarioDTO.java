package pe.edu.pucp.entrefunciones.rest.dto.usuarios;
//Es lo que Java le devuelve a C# cuando el login sale bien. Solo tiene id, correo y activo, sin nombres, DNI ni contraseña.
// Es un DTO aparte porque el login todavía no sabe si la persona es cliente o administrador, así que responde con
// Esta para darle a C# los datos que necesita para armar la sesión, sobre todo el id:
public class CuentaUsuarioDTO {
    private int id;
    private boolean activo;
    private String correo;

    public CuentaUsuarioDTO() {
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

}
