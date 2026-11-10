using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
    public interface ICuentaService
    {

        Task<bool> Autenticar(string correo, string contrasena);

        Task<CuentaUsuarioDTO> FindByCorreo(string correo);

 
        Task<string?> ObtenerRol(string correo);

       
        Task Recuperar(string correo, string dni, string contrasenaNueva);
    }
}
