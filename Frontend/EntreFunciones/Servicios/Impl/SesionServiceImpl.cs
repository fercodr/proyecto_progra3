using EntreFunciones.DTOSS.Usuarios;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using System.Security.Claims;

namespace EntreFunciones.Servicios.Impl
{
    public class SesionServiceImpl : ISesionService
    {
        private const string RutaInicioCliente = "/";
        private const string RutaInicioAdmin = "/admin";

        private readonly IHttpContextAccessor httpContextAccessor;
        private readonly ICuentaService cuentaService;

        public SesionServiceImpl(
            IHttpContextAccessor httpContextAccessor,
            ICuentaService cuentaService)
        {
            this.httpContextAccessor = httpContextAccessor;
            this.cuentaService = cuentaService;
        }

        public async Task<string> IniciarSesion(
            HttpContext context,
            string userName,
            string password)
        {
            SesionDTO? sesion = await cuentaService.Autenticar(userName, password);
            if (sesion is null
                || string.IsNullOrWhiteSpace(sesion.Token)
                || sesion.Id <= 0
                || string.IsNullOrWhiteSpace(sesion.Rol)
                || sesion.Vence <= DateTimeOffset.UtcNow)
            {
                return "/login?error=true";
            }

            List<Claim> claims = [
                new Claim(ClaimTypes.NameIdentifier, sesion.Id.ToString()),
                new Claim(ClaimTypes.Name, sesion.Correo),
                new Claim(ClaimTypes.Role, sesion.Rol),
                new Claim("access_token", sesion.Token),
                new Claim(
                    "access_token_expires",
                    sesion.Vence.ToUnixTimeSeconds().ToString())
            ];
            ClaimsIdentity identidad = new(
                claims,
                CookieAuthenticationDefaults.AuthenticationScheme);
            AuthenticationProperties propiedades = new()
            {
                IsPersistent = true,
                ExpiresUtc = sesion.Vence,
                AllowRefresh = false
            };
            await context.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                new ClaimsPrincipal(identidad),
                propiedades);

            return sesion.Rol == "Administrador"
                ? RutaInicioAdmin
                : RutaInicioCliente;
        }

        public async Task CerrarSesion(HttpContext context)
        {
            await context.SignOutAsync(
                CookieAuthenticationDefaults.AuthenticationScheme);
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
