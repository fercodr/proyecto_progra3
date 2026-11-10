using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class FuncionService
        : RestServiceBase<FuncionResumenDTO, FuncionViewModel>,
          IFuncionService
    {
        protected override string ClaveRuta => "Funciones";

        public FuncionService(
            IConfiguration config,
            AuthenticationStateProvider auth)
            : base(config, auth)
        {
        }

        protected override FuncionViewModel ToViewModel(
            FuncionResumenDTO dto)
        {
            return Convertir(dto);
        }

        internal static FuncionViewModel Convertir(FuncionResumenDTO dto)
        {
            return new FuncionViewModel
            {
                Id = dto.Id,
                Titulo = dto.Titulo,
                Fecha = dto.Fecha,
                HoraInicio = dto.HoraInicio,
                Sede = dto.Sede,
                Sala = dto.Sala,
                Estado = dto.Estado
            };
        }
    }
}
