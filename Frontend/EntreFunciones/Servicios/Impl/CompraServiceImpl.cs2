using EntreFunciones.DTOSS;
using EntreFunciones.Servicios.Rest;

namespace EntreFunciones.Servicios.Impl
{
    public class CompraServiceImpl : ICompraService
    {
        private string BaseUrlReservas { get; set; }
        private string BaseUrlPagos { get; set; }
        private string BaseUrlFunciones { get; set; }

        public CompraServiceImpl(IConfiguration configuration)
        {
            BaseUrlReservas = $"{configuration["RestApi:BaseUrl"]!}/reservas";
            BaseUrlPagos = $"{configuration["RestApi:BaseUrl"]!}/pagos";
            BaseUrlFunciones = $"{configuration["RestApi:BaseUrl"]!}/funciones";
        }

        public async Task<DisponibilidadDto> Disponibilidad(int funcionId)
        {
            string url = $"{BaseUrlFunciones}/{funcionId}/disponibilidad";
            return await HttpUtils.DoGet<DisponibilidadDto>(url);
        }

        public async Task<ReservaDto> Crear(int funcionId, IEnumerable<int> idsButacas, int cantidad)
        {
            string url = BaseUrlReservas;
            var payload = new { FuncionId = funcionId, IdsButacas = idsButacas, Cantidad = cantidad };
            return await HttpUtils.DoPost<object, ReservaDto>(url, payload);
        }

        public async Task<ReservaDto> Obtener(int reservaId)
        {
            string url = $"{BaseUrlReservas}/{reservaId}";
            return await HttpUtils.DoGet<ReservaDto>(url);
        }

        public async Task<PagoDto> GenerarPago(int reservaId)
        {
            string url = $"{BaseUrlReservas}/{reservaId}/pagos";
            return await HttpUtils.DoPost<object, PagoDto>(url, new {});
        }

        public async Task RegistrarPago(int pagoId)
        {
            string url = $"{BaseUrlPagos}/{pagoId}/registrar";
            await HttpUtils.DoPut<object, object>(url, new {});
        }

        public async Task Cancelar(int reservaId)
        {
            string url = $"{BaseUrlReservas}/{reservaId}/cancelar";
            await HttpUtils.DoPut<object, object>(url, new {});
        }

        public async Task<List<ReservaDto>> ListarReservas()
        {
            string url = BaseUrlReservas;
            return await HttpUtils.DoGet<List<ReservaDto>>(url);
        }

        public async Task<List<PagoDto>> ListarPagos()
        {
            string url = BaseUrlPagos;
            return await HttpUtils.DoGet<List<PagoDto>>(url);
        }

        public async Task Validar(int pagoId)
        {
            string url = $"{BaseUrlPagos}/{pagoId}/validar";
            await HttpUtils.DoPut<object, object>(url, new {});
        }

        public async Task Rechazar(int pagoId)
        {
            string url = $"{BaseUrlPagos}/{pagoId}/rechazar";
            await HttpUtils.DoPut<object, object>(url, new {});
        }
    }
}
