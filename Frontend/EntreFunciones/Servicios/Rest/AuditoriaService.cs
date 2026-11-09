using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;

namespace EntreFunciones.Servicios.Rest
{
    public class AuditoriaService : RestServiceBase<AuditoriaDTO, AuditoriaViewModel>, IAuditoriaService
    {
        protected override string ClaveRuta => "Auditorias";

        public AuditoriaService(IConfiguration config) : base(config)
        {
        }

        protected override AuditoriaViewModel ToViewModel(AuditoriaDTO dto)
        {
            return new AuditoriaViewModel
            {
                Id = dto.Id,
                FuncionId = dto.FuncionId,
                Fecha = dto.Fecha,
                Responsable = dto.Responsable,
                Descripcion = dto.Descripcion,
                EstadoAuditoria = dto.Estado
            };
        }

        protected override AuditoriaDTO ToDto(AuditoriaViewModel modelo)
        {
            return new AuditoriaDTO
            {
                Id = modelo.Id,
                FuncionId = modelo.FuncionId,
                Fecha = modelo.Fecha,
                Responsable = modelo.Responsable,
                Descripcion = modelo.Descripcion,
                Estado = modelo.EstadoAuditoria
            };
        }
    }
}
