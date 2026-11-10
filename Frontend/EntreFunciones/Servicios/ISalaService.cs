using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface ISalaService
    {
        Task<List<SalaViewModel>> FindAll();
        Task<List<SalaViewModel>> FindBySede(int idSede);
        Task<SalaViewModel> FindById(int id);
        Task Insert(SalaViewModel modelo);
        Task Update(SalaViewModel modelo);
        Task Delete(int id);
    }
}