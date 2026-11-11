using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface IAuditoriaService
    {
        Task<List<AuditoriaViewModel>> FindAll();
        Task<List<AuditoriaViewModel>> FilterBy(
            string? descripcion,
            EstadoAuditoria? estado);
        Task<AuditoriaViewModel> FindById(int id);
        Task<AuditoriaViewModel> Insert(AuditoriaViewModel modelo);
        Task<AuditoriaViewModel> Update(AuditoriaViewModel modelo);
    }
}
