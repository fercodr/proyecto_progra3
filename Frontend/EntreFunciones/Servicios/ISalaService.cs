using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface ISalaService
    {
        Task<List<SalaViewModel>> FindAll();
        Task<List<SalaViewModel>> FindBySede(int idSede);
        Task<SalaViewModel> FindById(int id);
        Task Insert(SalaViewModel modelo);
        Task<SalaViewModel> InsertConButacas(SalaViewModel modelo, List<ButacaViewModel> butacas);
        Task Update(SalaViewModel modelo);
        Task Delete(int id);
    }
}