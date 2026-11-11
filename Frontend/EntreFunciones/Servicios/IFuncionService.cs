using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface IFuncionService
    {
        Task<List<FuncionViewModel>> FindAll();
        Task<List<FuncionViewModel>> FindReportes(
            DateOnly desde,
            DateOnly hasta,
            int? idSede = null,
            int? idSala = null);
    }
}
