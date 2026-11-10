using EntreFunciones.DTOSS.Usuarios;

namespace EntreFunciones.Servicios.Rest
{
    public interface ICuentaService
    {
        // POST /cuentas/login -> la cuenta, o null si las credenciales son incorrectas (401)
        Task<bool> Autenticar(string correo, string contrasena);
        // GET /cuentas/{correo} -> datos de la cuenta (id, correo, activo)
        Task<CuentaUsuarioDTO> FindByCorreo(string correo);

        // GET /cuentas/{correo}/administrador y /cliente -> "Administrador", "Cliente" o null
        Task<string?> ObtenerRol(string correo);
    }
}
