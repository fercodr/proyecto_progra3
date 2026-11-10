using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface IButacaService
    {
        Task<List<ButacaViewModel>> FindAll();
        Task<List<ButacaViewModel>> FindBySala(int idSala);
        Task<ButacaViewModel> FindById(int id);
        Task Insert(ButacaViewModel modelo);
        Task Update(ButacaViewModel modelo);
        Task Delete(int id);
    }
}