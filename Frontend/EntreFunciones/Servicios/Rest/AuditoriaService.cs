using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class AuditoriaService
        : RestServiceBase<AuditoriaDTO, AuditoriaViewModel>,
          IAuditoriaService
    {
        protected override string ClaveRuta => "Auditorias";

        public AuditoriaService(
            IConfiguration config,
            AuthenticationStateProvider auth)
            : base(config, auth)
        {
        }

        public async Task<List<AuditoriaViewModel>> FilterBy(
            string? descripcion,
            EstadoAuditoria? estado)
        {
            List<KeyValuePair<string, string?>> parametros = new()
            {
                new KeyValuePair<string, string?>(
                    "descripcion",
                    descripcion?.Trim()),
                new KeyValuePair<string, string?>(
                    "estado",
                    estado?.ToString())
            };
            return await ConsultarModelos(parametros);
        }

        public async Task<AuditoriaViewModel> Insert(AuditoriaViewModel modelo)
        {
            CrearAuditoriaDTO payload = new()
            {
                FuncionId = modelo.FuncionId,
                ResponsableId = modelo.ResponsableId,
                Descripcion = modelo.Descripcion
            };
            return await base.Insert(payload);
        }

        public async Task<AuditoriaViewModel> Update(
            AuditoriaViewModel modelo)
        {
            ActualizarAuditoriaDTO payload = new()
            {
                ResponsableId = modelo.ResponsableId,
                Estado = modelo.EstadoAuditoria
            };
            return await base.Update(modelo.Id, payload);
        }

        protected override AuditoriaViewModel ToViewModel(
            AuditoriaDTO dto)
        {
            return new AuditoriaViewModel
            {
                Id = dto.Id,
                Funcion = dto.Funcion is null
                    ? null
                    : FuncionService.Convertir(dto.Funcion),
                Fecha = dto.Fecha,
                Responsable = dto.Responsable is null
                    ? null
                    : AdministradorService.Convertir(dto.Responsable),
                ResponsableTextoOriginal = dto.ResponsableTextoOriginal,
                Descripcion = dto.Descripcion,
                EstadoAuditoria = dto.Estado
            };
        }
    }
}
