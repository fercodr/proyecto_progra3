using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface ISedeService
    {
        Task<List<SedeViewModel>> FindAll();
        Task<SedeViewModel> FindById(int id);
        Task Insert(SedeViewModel modelo);
        Task Update(SedeViewModel modelo);
        Task Delete(int id);
    }
}
