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
        private readonly ILogger<SesionServiceImpl> logger;

        public SesionServiceImpl(
            IHttpContextAccessor httpContextAccessor,
            ICuentaService cuentaService,
            ILogger<SesionServiceImpl> logger)
        {
            this.httpContextAccessor = httpContextAccessor;
            this.cuentaService = cuentaService;
            this.logger = logger;
        }

        public async Task<string> IniciarSesion(
            HttpContext context,
            string userName,
            string password)
        {
            SesionDTO? sesion;
            try
            {
                sesion = await cuentaService.Autenticar(userName, password);
            }
            catch (ServiceException ex)
            {
                logger.LogError(ex,
                    "Fallo el servicio REST durante el inicio de sesion. "
                    + "Endpoint: {Endpoint}; estado HTTP: {StatusCode}; "
                    + "tipo de contenido: {ResponseContentType}",
                    ex.Endpoint,
                    ex.StatusCode,
                    ex.ResponseContentType);
                return "/login?servicioNoDisponible=true";
            }

            if (sesion is null)
            {
                return "/login?error=true";
            }

            if (string.IsNullOrWhiteSpace(sesion.Token)
                || sesion.Id <= 0
                || string.IsNullOrWhiteSpace(sesion.Correo)
                || string.IsNullOrWhiteSpace(sesion.Rol)
                || (sesion.Rol != "Administrador" && sesion.Rol != "Cliente")
                || sesion.Vence <= DateTimeOffset.UtcNow)
            {
                logger.LogError(
                    "El servicio REST devolvio una sesion incompleta o invalida; no se creo la cookie.");
                return "/login?servicioNoDisponible=true";
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
            try
            {
                await context.SignInAsync(
                    CookieAuthenticationDefaults.AuthenticationScheme,
                    new ClaimsPrincipal(identidad),
                    propiedades);
            }
            catch (Exception ex)
            {
                logger.LogError(ex,
                    "No se pudo crear la cookie de sesion para la cuenta {CuentaId}.",
                    sesion.Id);
                return "/login?errorSesion=true";
            }

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
