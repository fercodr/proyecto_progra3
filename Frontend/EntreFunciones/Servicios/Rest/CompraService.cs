using EntreFunciones.DTOSS;
using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;

namespace EntreFunciones.Servicios.Rest
{
    public class CompraService : ICompraService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public CompraService(IConfiguration config, AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException("Falta configurar Rest:BaseUrl.");
        }

        public async Task<DisponibilidadDto> Disponibilidad(int funcionId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"funciones/{funcionId}/disponibilidad");
            return await HttpUtils.DoGet<DisponibilidadDto>(url, cabeceras: cabeceras);
        }

        public async Task<ReservaDto> Crear(
            int funcionId,
            IEnumerable<int> idsButacas,
            int cantidad)
        {
            CrearReservaRequestDto solicitud = new()
            {
                FuncionId = funcionId,
                ButacaIds = idsButacas.ToList(),
                Cantidad = cantidad
            };
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl("reservas");

            return await HttpUtils.DoPost<CrearReservaRequestDto, ReservaDto>(
                url,
                solicitud,
                cabeceras: cabeceras);
        }

        public async Task<ReservaDto> Obtener(int reservaId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"reservas/{reservaId}");
            return await HttpUtils.DoGet<ReservaDto>(url, cabeceras: cabeceras);
        }

        public async Task<PagoDto> GenerarPago(int reservaId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"reservas/{reservaId}/pago");
            return await HttpUtils.DoPost<PagoDto>(url, cabeceras: cabeceras);
        }

        public async Task RegistrarPago(int pagoId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"pagos/{pagoId}/registrar");
            await HttpUtils.DoPost(url, cabeceras: cabeceras);
        }

        public async Task Cancelar(int reservaId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"reservas/{reservaId}");
            await HttpUtils.DoDelete(url, cabeceras: cabeceras);
        }

        public async Task<List<ReservaDto>> ListarReservas()
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl("reservas");
            return await HttpUtils.DoGet<List<ReservaDto>>(url, cabeceras: cabeceras);
        }

        public async Task<List<PagoDto>> ListarPagos()
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl("pagos");
            return await HttpUtils.DoGet<List<PagoDto>>(url, cabeceras: cabeceras);
        }

        public async Task Validar(int pagoId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"pagos/{pagoId}/validar");
            await HttpUtils.DoPost(url, cabeceras: cabeceras);
        }

        public async Task Rechazar(int pagoId)
        {
            IReadOnlyDictionary<string, string> cabeceras = await CrearCabeceras();
            string url = CrearUrl($"pagos/{pagoId}/rechazar");
            await HttpUtils.DoPost(url, cabeceras: cabeceras);
        }

        private string CrearUrl(string ruta)
        {
            return $"{_baseUrl.TrimEnd('/')}/{ruta.TrimStart('/')}";
        }

        private async Task<IReadOnlyDictionary<string, string>> CrearCabeceras()
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

            string? apiKey = _config["Rest:ApiKey"];
            if (string.IsNullOrWhiteSpace(apiKey))
            {
                throw new InvalidOperationException("Falta configurar el acceso al servicio REST.");
            }

            // El backend actual usa autenticación entre servidores con estas dos cabeceras.
            // TODO JWT: migrar solo cuando AccesoRest.java acepte y valide tokens JWT.
            return new Dictionary<string, string>
            {
                ["X-Api-Key"] = apiKey,
                ["X-Usuario-Id"] = cuentaId.ToString()
            };
        }
    }
}
