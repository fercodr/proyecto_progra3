using EntreFunciones.DTOSS.Usuarios;
using EntreFunciones.Servicios.Rest;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using System.Security.Claims;

namespace EntreFunciones.Servicios.Impl
{
    public class SesionServiceImpl : ISesionService
    {
        // Según el documento: el login lleva a "/" (cliente) o a "/admin" (administrador, pantalla Reportes).
        // Si aún no existe la pantalla Reportes, cambien RutaInicioAdmin a "/admin/usuarios".
        private const string RutaInicioCliente = "/";
        private const string RutaInicioAdmin = "/admin";

        private readonly IHttpContextAccessor httpContextAccessor;
        private readonly ICuentaService cuentaService;

        public SesionServiceImpl(IHttpContextAccessor httpContextAccessor, ICuentaService cuentaService)
        {
            this.httpContextAccessor = httpContextAccessor;
            this.cuentaService = cuentaService;
        }

        public async Task<string> IniciarSesion(HttpContext context, string userName, string password)
        {
            // Credenciales correctas
            bool autenticado = await cuentaService.Autenticar(userName, password);
            if (!autenticado)
            {
                return "/login?error=true";
            }

            // Datos de la cuenta (para el id)
            CuentaUsuarioDTO cuenta = await cuentaService.FindByCorreo(userName);

            // Qué rol tiene?
            string? rol = await cuentaService.ObtenerRol(userName);
            if (rol == null)
            {
                return "/login?error=true";
            }

            List<Claim> claims = [
                new Claim(ClaimTypes.NameIdentifier, cuenta.Id.ToString()),
                new Claim(ClaimTypes.Name, cuenta.Correo),
                new Claim(ClaimTypes.Role, rol)
            ];
            ClaimsIdentity identidad = new(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            await context.SignInAsync(new ClaimsPrincipal(identidad));

            return rol == "Administrador" ? RutaInicioAdmin : RutaInicioCliente;
        }

        public async Task CerrarSesion(HttpContext context)
        {
            await context.SignOutAsync();
        }

        public string ObtenerUserName()
        {
            ClaimsPrincipal usuario = httpContextAccessor.HttpContext!.User;
            return usuario.Identity!.Name!;
        }

        public int ObtenerIdCuentaUsuario()
        {
            ClaimsPrincipal usuario = httpContextAccessor.HttpContext!.User;
            return int.Parse(usuario.FindFirst(ClaimTypes.NameIdentifier)!.Value);
        }

    }
}
