using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios.Rest
{
    public interface IAuditoriaService
    {
        Task<List<AuditoriaViewModel>> FindAll(
            string? descripcion = null,
            EstadoAuditoria? estado = null);
        Task<AuditoriaViewModel> FindById(int id);
        Task<AuditoriaViewModel> Insert(NuevaAuditoriaFormModel formulario);
        Task<AuditoriaViewModel> Update(
            int id,
            AtencionAuditoriaFormModel formulario);
        Task<List<AdministradorViewModel>> FindResponsablesActivos();
        Task<List<FuncionViewModel>> FindFuncionesAsignables();
    }
}
