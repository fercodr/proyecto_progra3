using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios.Rest
{
    public interface IFuncionService
    {
        Task<List<FuncionViewModel>> FindAll();
    }
}
