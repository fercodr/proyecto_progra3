using EntreFunciones.DTOSS;
using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;

namespace EntreFunciones.Servicios.Rest
{
    public class CatalogoService : ICatalogoService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public CatalogoService(IConfiguration config, AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        public async Task<List<EventoDTO>> ListarEventos()
        {
            IReadOnlyDictionary<string, string> cabeceras = CrearCabecerasPublicas();
            string url = CrearUrl("eventos");
            return await HttpUtils.DoGet<List<EventoDTO>>(url, cabeceras: cabeceras);
        }

        public async Task<EventoDTO> ObtenerEvento(int id, bool completo = false)
        {
            IReadOnlyDictionary<string, string> cabeceras = CrearCabecerasPublicas();
            string url = CrearUrl($"eventos/{id}" + (completo ? "?completo=true" : ""));
            return await HttpUtils.DoGet<EventoDTO>(url, cabeceras: cabeceras);
        }

        public async Task<List<FuncionDTO>> ListarFunciones()
        {
            IReadOnlyDictionary<string, string> cabeceras = CrearCabecerasPublicas();
            string url = CrearUrl("funciones");
            return await HttpUtils.DoGet<List<FuncionDTO>>(url, cabeceras: cabeceras);
        }

        public async Task<EventoDTO> CrearEvento(EventoDTO evento)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabecerasAdministrador();
            string url = CrearUrl("eventos");
            return await HttpUtils.DoPost<EventoDTO, EventoDTO>(url, evento, cabeceras: cabeceras);
        }

        public async Task<EventoDTO> ActualizarEvento(EventoDTO evento)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabecerasAdministrador();
            string url = CrearUrl($"eventos/{evento.Id}");
            return await HttpUtils.DoPut<EventoDTO, EventoDTO>(url, evento, cabeceras: cabeceras);
        }

        public async Task EliminarEvento(int id)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabecerasAdministrador();
            string url = CrearUrl($"eventos/{id}");
            await HttpUtils.DoDelete(url, cabeceras: cabeceras);
        }

        public async Task<FuncionDTO> ProgramarFuncion(FuncionDTO funcion)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabecerasAdministrador();
            string url = CrearUrl("funciones");
            return await HttpUtils.DoPost<FuncionDTO, FuncionDTO>(url, funcion, cabeceras: cabeceras);
        }

        public async Task<FuncionDTO> ActualizarFuncion(FuncionDTO funcion)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabecerasAdministrador();
            string url = CrearUrl($"funciones/{funcion.Id}");
            return await HttpUtils.DoPut<FuncionDTO, FuncionDTO>(url, funcion, cabeceras: cabeceras);
        }

        public async Task CancelarFuncion(int id)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabecerasAdministrador();
            string url = CrearUrl($"funciones/{id}");
            await HttpUtils.DoDelete(url, cabeceras: cabeceras);
        }

        private string CrearUrl(string ruta)
        {
            return $"{_baseUrl.TrimEnd('/')}/{ruta.TrimStart('/')}";
        }

        /// <summary>
        /// AccesoRest.java (filtro global del backend) exige X-Api-Key + X-Usuario-Id en TODAS las
        /// rutas, sin excepcion para lectura publica. Cartelera/Detalle son publicas (no se exige
        /// login al visitante, ver MainLayout.razor), asi que estas lecturas usan una cuenta de
        /// servicio fija (Rest:CuentaServicioId) en vez de la identidad del visitante, que no existe
        /// cuando no inicio sesion. Ver decisiones-diseno-catalogo-rest.md en el proyecto de Claude.
        /// </summary>
        private IReadOnlyDictionary<string, string> CrearCabecerasPublicas()
        {
            string apiKey = ObtenerApiKey();
            string cuentaServicioId = _config["Rest:CuentaServicioId"]
                ?? throw new InvalidOperationException(
                    "Falta configurar Rest:CuentaServicioId (id de un usuario activo en la BD, para las lecturas publicas del catalogo).");

            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = apiKey,
                ["X-Usuario-Id"] = cuentaServicioId
            };
        }

        private async Task<IReadOnlyDictionary<string, string>> CrearCabecerasAdministrador()
        {
            AuthenticationState estadoAutenticacion = await _auth.GetAuthenticationStateAsync();
            ClaimsPrincipal usuario = estadoAutenticacion.User;
            string? idUsuario = usuario.FindFirst(ClaimTypes.NameIdentifier)?.Value;

            if (usuario.Identity?.IsAuthenticated != true
                || !int.TryParse(idUsuario, out int cuentaId)
                || cuentaId <= 0)
            {
                throw new InvalidOperationException("Inicia sesión para continuar.");
            }

            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = ObtenerApiKey(),
                ["X-Usuario-Id"] = cuentaId.ToString()
            };
        }

        private string ObtenerApiKey()
        {
            string? apiKey = _config["Rest:ApiKey"];
            if (string.IsNullOrWhiteSpace(apiKey))
            {
                throw new InvalidOperationException("Falta configurar el acceso al servicio REST.");
            }
            return apiKey;
        }
    }
}
