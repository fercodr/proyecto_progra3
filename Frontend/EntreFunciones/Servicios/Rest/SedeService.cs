using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;

namespace EntreFunciones.Servicios.Rest
{
    public class SedeService : RestServiceBase<SedeDto, SedeViewModel>, ISedeService
    {
        protected override string ClaveRuta => "Sedes";

        public SedeService(
            IConfiguration config,
            AuthenticationStateProvider auth) : base(config, auth)
        {
        }

        public async Task Insert(SedeViewModel modelo)
        {
            await base.Insert(ToDto(modelo));
        }

        public async Task Update(SedeViewModel modelo)
        {
            await base.Update(modelo.Id, ToDto(modelo));
        }

        public new async Task Delete(int id)
        {
            await base.Delete(id);
        }

        protected override SedeViewModel ToViewModel(SedeDto dto)
        {
            return new SedeViewModel
            {
                Id = dto.Id,
                IsActivo = dto.IsActivo,
                RazonSocial = dto.RazonSocial,
                Direccion = dto.Direccion,
                Ciudad = dto.Ciudad,
                TelefonoContacto = dto.TelefonoContacto ?? string.Empty
            };
        }

        private static SedeDto ToDto(SedeViewModel modelo)
        {
            return new SedeDto
            {
                Id = modelo.Id,
                IsActivo = modelo.IsActivo,
                RazonSocial = modelo.RazonSocial,
                Direccion = modelo.Direccion,
                Ciudad = modelo.Ciudad,
                TelefonoContacto = modelo.TelefonoContacto
            };
        }
    }
}
