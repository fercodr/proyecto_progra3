using EntreFunciones.DTOSS;

namespace EntreFunciones.Servicios.Rest
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
        Task<List<PagoDto>> ListarPagos();
        Task Validar(int pagoId);
        Task Rechazar(int pagoId);
    }
}
