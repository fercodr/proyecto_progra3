using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios
{
    public interface IAdministradorService
    {
        Task<List<AdministradorViewModel>> FindAll();
    }
}
