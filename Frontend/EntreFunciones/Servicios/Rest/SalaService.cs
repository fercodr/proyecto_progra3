using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;
using System.Globalization;

namespace EntreFunciones.Servicios.Rest
{
    public class SalaService : RestServiceBase<SalaDto, SalaViewModel>, ISalaService
    {
        private readonly ISedeService _sedeService;

        protected override string ClaveRuta => "Salas";

        public SalaService(
            IConfiguration config,
            AuthenticationStateProvider auth,
            ISedeService sedeService) : base(config, auth)
        {
            _sedeService = sedeService;
        }

        public override async Task<List<SalaViewModel>> FindAll()
        {
            List<SalaViewModel> salas = await base.FindAll();
            await CompletarNombreSede(salas);
            return salas;
        }

        public override async Task<SalaViewModel> FindById(int id)
        {
            SalaViewModel sala = await base.FindById(id);
            await CompletarNombreSede([sala]);
            return sala;
        }

        public async Task<List<SalaViewModel>> FindBySede(int idSede)
        {
            List<SalaViewModel> salas = await ConsultarModelos(
                new Dictionary<string, string?>
                {
                    ["idSede"] = idSede.ToString(CultureInfo.InvariantCulture)
                });
            await CompletarNombreSede(salas);
            return salas;
        }

        public async Task Insert(SalaViewModel modelo)
        {
            await base.Insert(ToDto(modelo));
        }

        public async Task Update(SalaViewModel modelo)
        {
            await base.Update(modelo.Id, ToDto(modelo));
        }

        public new async Task Delete(int id)
        {
            await base.Delete(id);
        }

        // El backend solo envía idSede: el nombre de la sede se obtiene del servicio de sedes
        private async Task CompletarNombreSede(List<SalaViewModel> salas)
        {
            if (salas.Count == 0)
            {
                return;
            }

            List<SedeViewModel> sedes = await _sedeService.FindAll();
            Dictionary<int, string> nombres = sedes.ToDictionary(s => s.Id, s => s.RazonSocial);
            foreach (SalaViewModel sala in salas)
            {
                sala.NombreSede = nombres.GetValueOrDefault(sala.IdSede, string.Empty);
            }
        }

        protected override SalaViewModel ToViewModel(SalaDto dto)
        {
            return new SalaViewModel
            {
                Id = dto.Id,
                IsActivo = dto.IsActivo,
                IdSede = dto.IdSede,
                Nombre = dto.Nombre,
                Aforo = dto.Aforo,
                TipoSala = dto.TipoSala
            };
        }

        private static SalaDto ToDto(SalaViewModel modelo)
        {
            return new SalaDto
            {
                Id = modelo.Id,
                IsActivo = modelo.IsActivo,
                IdSede = modelo.IdSede,
                Nombre = modelo.Nombre,
                Aforo = modelo.Aforo,
                TipoSala = modelo.TipoSala
            };
        }
    }
}
