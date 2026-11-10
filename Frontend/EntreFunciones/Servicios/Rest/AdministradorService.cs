using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class AdministradorService
        : RestServiceBase<AdministradorDTO, AdministradorViewModel>,
          IAdministradorService
    {
        protected override string ClaveRuta => "Administradores";

        public AdministradorService(
            IConfiguration config,
            AuthenticationStateProvider auth)
            : base(config, auth)
        {
        }

        protected override AdministradorViewModel ToViewModel(
            AdministradorDTO dto)
        {
            return Convertir(dto);
        }

        internal static AdministradorViewModel Convertir(
            AdministradorDTO dto)
        {
            return new AdministradorViewModel
            {
                Id = dto.Id,
                IsActivo = dto.Activo,
                Nombres = dto.Nombres,
                Apellidos = dto.Apellidos
            };
        }
    }
}
