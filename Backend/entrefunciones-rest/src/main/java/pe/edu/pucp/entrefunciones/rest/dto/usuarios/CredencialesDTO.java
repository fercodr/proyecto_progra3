//Lo que manda el login: correo y contraseña
//No es necesario un mapper porque solo vamos a leer sus datos desde el servicio, no tenemos que guardarlo porque estamos en el Login, esos datos ya estan guardados, solo los recibimos para compararlos
package pe.edu.pucp.entrefunciones.rest.dto.usuarios;

public class CredencialesDTO {
    private String correo;
    private String contrasena;

    public CredencialesDTO() {
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
}
