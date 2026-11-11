package pe.edu.pucp.entrefunciones.rest.dto.usuarios;

public class SesionDTO {
    private String token;
    private String vence;
    private int id;
    private String correo;
    private String rol;

    public SesionDTO() {
    }

    public SesionDTO(String token, String vence, int id, String correo, String rol) {
        this.token = token;
        this.vence = vence;
        this.id = id;
        this.correo = correo;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getVence() { return vence; }
    public void setVence(String vence) { this.vence = vence; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
