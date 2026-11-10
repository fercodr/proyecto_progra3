using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;
using System.Globalization;

namespace EntreFunciones.Servicios.Rest
{
    public class ButacaService : RestServiceBase<ButacaDto, ButacaViewModel>, IButacaService
    {
        protected override string ClaveRuta => "Butacas";

        public ButacaService(
            IConfiguration config,
            AuthenticationStateProvider auth) : base(config, auth)
        {
        }

        public async Task<List<ButacaViewModel>> FindBySala(int idSala)
        {
            return await ConsultarModelos(
                new Dictionary<string, string?>
                {
                    ["idSala"] = idSala.ToString(CultureInfo.InvariantCulture)
                });
        }

        public async Task Insert(ButacaViewModel modelo)
        {
            await base.Insert(ToDto(modelo));
        }

        public async Task Update(ButacaViewModel modelo)
        {
            await base.Update(modelo.Id, ToDto(modelo));
        }

        public new async Task Delete(int id)
        {
            await base.Delete(id);
        }

        protected override ButacaViewModel ToViewModel(ButacaDto dto)
        {
            return new ButacaViewModel
            {
                Id = dto.Id,
                IsActivo = dto.IsActivo,
                IdSala = dto.IdSala,
                Fila = dto.Fila,
                Columna = dto.Columna,
                TipoButaca = dto.TipoButaca
            };
        }

        private static ButacaDto ToDto(ButacaViewModel modelo)
        {
            return new ButacaDto
            {
                Id = modelo.Id,
                IsActivo = modelo.IsActivo,
                IdSala = modelo.IdSala,
                Fila = modelo.Fila,
                Columna = modelo.Columna,
                TipoButaca = modelo.TipoButaca
            };
        }
    }
}