using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using EntreFunciones.Servicios;
using EntreFunciones.Servicios.Impl;
namespace EntreFunciones.Seguridad
{
    public static class SeguridadExtensions
    {
        public static void AddSeguridad(this IServiceCollection services)
        {
            services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
                .AddCookie(opciones => {
                    opciones.LoginPath = "/login";
                    opciones.AccessDeniedPath = "/acceso-denegado";
                    opciones.ExpireTimeSpan = TimeSpan.FromHours(8);
                });

            services.AddAuthorization(opciones => {
                foreach (KeyValuePair<string, string[]> permiso in Permisos.RolesPorPermiso)
                {
                    opciones.AddPolicy(permiso.Key,
                        politica => politica.RequireRole(permiso.Value));
                }
            });

            // Esta línea es la que hace que <AuthorizeView> y "context.User" funcionen en las páginas.
            services.AddCascadingAuthenticationState();
            services.AddHttpContextAccessor();
            services.AddScoped<ISesionService, SesionServiceImpl>();
        }

        public static void MapSesionEndpoints(this IEndpointRouteBuilder app)
        {
            app.MapPost("/cuenta/login", IniciarSesion);
            app.MapPost("/cuenta/logout", CerrarSesion);
        }

        private static async Task<IResult> IniciarSesion(HttpContext context, ISesionService sesionService,
                [FromForm] string userName, [FromForm] string password)
        {
            string destino = await sesionService.IniciarSesion(context, userName, password);
            return Results.Redirect(destino);
        }

        private static async Task<IResult> CerrarSesion(HttpContext context, ISesionService sesionService)
        {
            await sesionService.CerrarSesion(context);
            return Results.Redirect("/login");
        }
    }
}
