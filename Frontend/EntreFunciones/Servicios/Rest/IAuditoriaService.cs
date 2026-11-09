using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios.Rest
{
    public interface IAuditoriaService
    {
        Task<List<AuditoriaViewModel>> FindAll();
        Task<AuditoriaViewModel> FindById(int id);
        Task Insert(AuditoriaViewModel modelo);
        Task Update(AuditoriaViewModel modelo);
        Task Delete(int id);
    }
}
