package pe.edu.pucp.entrefunciones.modelo.usuarios;
public interface IAutenticable{
    boolean validarCredenciales(String correo, String contrasena);
    void recuperarContrasena();
}
