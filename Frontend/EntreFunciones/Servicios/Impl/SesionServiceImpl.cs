using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using System.Security.Claims;

namespace EntreFunciones.Servicios.Impl
{
    // VERSIÓN DEMO: no usa BL ni BD. Dos usuarios fijos, contraseña "123456".
    // Cuando se conecte al BL real, se reemplaza el cuerpo de IniciarSesion por la validación real.

    public class SesionServiceImpl : ISesionService
    {
        // Según el documento: el login lleva a "/" (cliente) o a "/admin" (administrador, pantalla Reportes).
        // Si aún no existe la pantalla Reportes, cambien RutaInicioAdmin a "/admin/usuarios".
        private const string RutaInicioCliente = "/";
        private const string RutaInicioAdmin = "/admin";

        private readonly IHttpContextAccessor httpContextAccessor;

        public SesionServiceImpl(IHttpContextAccessor httpContextAccessor)
        {
            this.httpContextAccessor = httpContextAccessor;
        }

        public async Task<string> IniciarSesion(HttpContext context, string userName, string password)
        {
            if (password != "123456")
            {
                return "/login?error=true";
            }

            string? rol = userName switch
            {
                "admin@entrefunciones.com" => "Administrador",
                "cliente@entrefunciones.com" => "Cliente",
                _ => null
            };
            if (rol == null)
            {
                return "/login?error=true";
            }

            List<Claim> claims = [
                new Claim(ClaimTypes.NameIdentifier, rol == "Administrador" ? "1" : "2"),
                new Claim(ClaimTypes.Name, userName),
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
