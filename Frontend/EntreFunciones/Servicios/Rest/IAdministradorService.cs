using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios.Rest
{
    public interface IAdministradorService
    {
        Task<List<AdministradorViewModel>> FindAll();
    }
}
