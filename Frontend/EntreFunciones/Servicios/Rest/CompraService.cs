using EntreFunciones.DTOSS;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class CompraService : ICompraService
    {
        private readonly IConfiguration _config;
        private readonly AuthenticationStateProvider _auth;
        private readonly string _baseUrl;

        public CompraService(
            IConfiguration config,
            AuthenticationStateProvider auth)
        {
            _config = config;
            _auth = auth;
            _baseUrl = config["Rest:BaseUrl"]
                ?? throw new InvalidOperationException(
                    "Falta configurar Rest:BaseUrl.");
        }

        public async Task<DisponibilidadDto> Disponibilidad(int funcionId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"funciones/{funcionId}/disponibilidad");
            return await HttpUtils.DoGet<DisponibilidadDto>(
                url,
                cabeceras: cabeceras);
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
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl("reservas");

            return await HttpUtils.DoPost<CrearReservaRequestDto, ReservaDto>(
                url,
                solicitud,
                cabeceras: cabeceras);
        }

        public async Task<ReservaDto> Obtener(int reservaId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"reservas/{reservaId}");
            return await HttpUtils.DoGet<ReservaDto>(
                url,
                cabeceras: cabeceras);
        }

        public async Task<PagoDto> GenerarPago(int reservaId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"reservas/{reservaId}/pago");
            return await HttpUtils.DoPost<PagoDto>(
                url,
                cabeceras: cabeceras);
        }

        public async Task RegistrarPago(int pagoId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"pagos/{pagoId}/registrar");
            await HttpUtils.DoPost(url, cabeceras: cabeceras);
        }

        public async Task Cancelar(int reservaId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"reservas/{reservaId}");
            await HttpUtils.DoDelete(url, cabeceras: cabeceras);
        }

        public async Task<List<ReservaDto>> ListarReservas()
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl("reservas");
            return await HttpUtils.DoGetList<ReservaDto>(
                url,
                cabeceras: cabeceras);
        }

        public async Task<List<PagoDto>> ListarPagos()
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl("pagos");
            return await HttpUtils.DoGetList<PagoDto>(
                url,
                cabeceras: cabeceras);
        }

        public async Task Validar(int pagoId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"pagos/{pagoId}/validar");
            await HttpUtils.DoPost(url, cabeceras: cabeceras);
        }

        public async Task Rechazar(int pagoId)
        {
            IReadOnlyDictionary<string, string> cabeceras =
                await CrearCabeceras();
            string url = CrearUrl($"pagos/{pagoId}/rechazar");
            await HttpUtils.DoPost(url, cabeceras: cabeceras);
        }

        private string CrearUrl(string ruta)
        {
            return HttpUtils.ConstruirUrl(_baseUrl, ruta);
        }

        private async Task<IReadOnlyDictionary<string, string>>
            CrearCabeceras()
        {
            return await HttpUtils.CrearCabecerasAutenticadas(
                _config,
                _auth);
        }
    }
}
