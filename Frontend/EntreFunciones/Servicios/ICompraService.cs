using EntreFunciones.DTOSS;

namespace EntreFunciones.Servicios
{
    public interface ICompraService
    {
        Task<DisponibilidadDto> Disponibilidad(int funcionId);
        Task<ReservaDto> Crear(int funcionId, IEnumerable<int> idsButacas, int cantidad);
        Task<ReservaDto> Obtener(int reservaId);
        Task<PagoDto> GenerarPago(int reservaId);
        Task RegistrarPago(int pagoId);
        Task Cancelar(int reservaId);
        Task<List<ReservaDto>> ListarReservas();
        Task<List<EntradaDto>> ListarEntradas(int reservaId);
        Task ValidarIngreso(string codigoQR);
        Task CancelarEntrada(int entradaId);
        Task<List<PagoDto>> ListarPagos();
        Task<PagoDto> BuscarPagoPorCodigo(string codigoPago);
        Task Validar(int pagoId);
        Task Rechazar(int pagoId);
    }
}
