using EntreFunciones.DTOSS;
using EntreFunciones.ViewModels;
using Microsoft.AspNetCore.Components.Authorization;
using System.Globalization;

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

        public async Task<List<FuncionViewModel>> FindReportes(
            DateOnly desde,
            DateOnly hasta,
            int? idSede = null,
            int? idSala = null)
        {
            if (desde > hasta)
            {
                throw new ArgumentException(
                    "La fecha desde no puede ser posterior a hasta.");
            }

            Dictionary<string, string?> parametros = new()
            {
                ["desde"] = desde.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture),
                ["hasta"] = hasta.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture),
                ["sedeId"] = idSede?.ToString(CultureInfo.InvariantCulture),
                ["salaId"] = idSala?.ToString(CultureInfo.InvariantCulture)
            };

            string url = HttpUtils.AgregarParametrosConsulta(
                HttpUtils.ConstruirUrl(
                    ConstruirUrlParaRuta("FuncionesReportes"), "reportes"),
                parametros);
            List<FuncionDTO> dtos = await ConsultarLista<FuncionDTO>(url);
            return dtos.Select(Convertir).ToList();
        }

        internal static FuncionViewModel Convertir(FuncionDTO dto)
        {
            DateOnly.TryParse(dto.Fecha, out DateOnly fecha);
            TimeOnly.TryParse(dto.HoraInicio, out TimeOnly horaInicio);
            FuncionViewModel modelo = new FuncionViewModel
            {
                Id = dto.Id,
                Titulo = dto.TituloEvento ?? string.Empty,
                Fecha = fecha,
                HoraInicio = horaInicio,
                Sede = dto.NombreSede ?? string.Empty,
                Sala = dto.NombreSala ?? string.Empty,
                Estado = dto.Estado ?? string.Empty
            };
            modelo.Evento = dto.Evento is null ? null : new EventoViewModel
            {
                Id = dto.Evento.Id,
                IsActivo = dto.Evento.Activo,
                Titulo = dto.Evento.Titulo,
                Tipo = dto.Evento.Tipo,
                Precio = (decimal)dto.Evento.Precio
            };
            modelo.SedeRelacionada = ToViewModel(dto.Sede);
            modelo.SalaRelacionada = ToViewModel(dto.Sala);
            modelo.Aforo = dto.Sala?.Aforo ?? 0;
            modelo.EntradasNoCanceladas = dto.EntradasNoCanceladas;
            modelo.EntradasCanceladas = dto.EntradasCanceladas;
            modelo.IngresosValidos = dto.IngresosValidos;
            modelo.Asistentes = dto.Asistentes;
            modelo.PorcentajeOcupacion = dto.PorcentajeOcupacion;
            if (modelo.SalaRelacionada is not null)
            {
                modelo.SalaRelacionada.Sede = modelo.SedeRelacionada;
            }
            return modelo;
        }

        private static SedeViewModel? ToViewModel(SedeDto? dto)
        {
            return dto is null ? null : new SedeViewModel
            {
                Id = dto.Id,
                IsActivo = dto.IsActivo,
                RazonSocial = dto.RazonSocial,
                Direccion = dto.Direccion,
                Ciudad = dto.Ciudad,
                TelefonoContacto = dto.TelefonoContacto ?? string.Empty
            };
        }

        private static SalaViewModel? ToViewModel(SalaDto? dto)
        {
            return dto is null ? null : new SalaViewModel
            {
                Id = dto.Id,
                IsActivo = dto.IsActivo,
                IdSede = dto.IdSede,
                Nombre = dto.Nombre,
                Aforo = dto.Aforo,
                TipoSala = dto.TipoSala
            };
        }
    }
}
