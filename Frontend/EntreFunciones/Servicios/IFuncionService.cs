using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface IFuncionService
    {
        Task<List<FuncionViewModel>> FindAll();
        Task<List<FuncionViewModel>> FindReportes(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede = null,
            int? idSala = null,
            string? tipoEvento = null);
        Task<byte[]> DescargarReporteOcupacion(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede = null,
            int? idSala = null);
        Task<byte[]> DescargarReporteIngresos(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede = null,
            string? tipoEvento = null);
        Task<byte[]> DescargarReporteDetalleOcupacion(int idEvento);
        Task<byte[]> DescargarReporteDetalleIngresos(int idEvento);
    }
}
