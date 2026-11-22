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
        private static readonly HashSet<string> TiposEventoReporte = new(
            ["PELICULA", "OBRA_TEATRO", "ACTIVIDAD_CULTURAL"],
            StringComparer.Ordinal);

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
                HoraFin = TimeOnly.MinValue,
                Sede = dto.Sede,
                Sala = dto.Sala,
                Estado = dto.Estado
            };
        }

        public async Task<List<FuncionViewModel>> FindReportes(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede = null,
            int? idSala = null,
            string? tipoEvento = null)
        {
            Dictionary<string, string?> parametros = ParametrosReporte(
                desde, hasta, idSede, idSala, tipoEvento);
            string url = UrlReporte("reportes", parametros);
            List<FuncionDTO> dtos = await ConsultarLista<FuncionDTO>(url);
            return dtos.Select(Convertir).ToList();
        }

        public async Task<byte[]> DescargarReporteOcupacion(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede = null,
            int? idSala = null)
        {
            Dictionary<string, string?> parametros = ParametrosReporte(
                desde, hasta, idSede, idSala, null);
            return await ConsultarBytes(
                UrlReporte("reportes/ocupacion/pdf", parametros));
        }

        public async Task<byte[]> DescargarReporteIngresos(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede = null,
            string? tipoEvento = null)
        {
            Dictionary<string, string?> parametros = ParametrosReporte(
                desde, hasta, idSede, null, tipoEvento);
            return await ConsultarBytes(
                UrlReporte("reportes/ingresos/pdf", parametros));
        }

        private string UrlReporte(
            string subruta,
            Dictionary<string, string?> parametros)
        {
            return HttpUtils.AgregarParametrosConsulta(
                HttpUtils.ConstruirUrl(
                    ConstruirUrlParaRuta("FuncionesReportes"), subruta),
                parametros);
        }

        private static Dictionary<string, string?> ParametrosReporte(
            DateOnly? desde,
            DateOnly? hasta,
            int? idSede,
            int? idSala,
            string? tipoEvento)
        {
            if (desde.HasValue && hasta.HasValue && desde.Value > hasta.Value)
            {
                throw new ArgumentException(
                    "La fecha desde no puede ser posterior a hasta.");
            }
            if (idSede is <= 0)
            {
                throw new ArgumentOutOfRangeException(nameof(idSede));
            }
            if (idSala is <= 0)
            {
                throw new ArgumentOutOfRangeException(nameof(idSala));
            }

            string? tipoEventoNormalizado = string.IsNullOrWhiteSpace(tipoEvento)
                ? null
                : tipoEvento.Trim();
            if (tipoEventoNormalizado is not null
                && !TiposEventoReporte.Contains(tipoEventoNormalizado))
            {
                throw new ArgumentException(
                    "El tipo de actividad no es válido.",
                    nameof(tipoEvento));
            }

            return new Dictionary<string, string?>
            {
                ["desde"] = desde?.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture),
                ["hasta"] = hasta?.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture),
                ["sedeId"] = idSede?.ToString(CultureInfo.InvariantCulture),
                ["salaId"] = idSala?.ToString(CultureInfo.InvariantCulture),
                ["tipoEvento"] = tipoEventoNormalizado
            };
        }

        internal static FuncionViewModel Convertir(FuncionDTO dto)
        {
            DateOnly.TryParse(dto.Fecha, out DateOnly fecha);
            TimeOnly.TryParse(dto.HoraInicio, out TimeOnly horaInicio);
            TimeOnly.TryParse(dto.HoraFin, out TimeOnly horaFin);
            FuncionViewModel modelo = new FuncionViewModel
            {
                Id = dto.Id,
                Titulo = dto.TituloEvento ?? string.Empty,
                Fecha = fecha,
                HoraInicio = horaInicio,
                HoraFin = horaFin,
                Idioma = dto.Idioma ?? string.Empty,
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
            modelo.EntradasVigentes = dto.EntradasVigentes;
            modelo.EntradasEmitidas = dto.EntradasEmitidas;
            modelo.EntradasCanceladas = dto.EntradasCanceladas;
            modelo.IngresosConfirmados = dto.IngresosConfirmados;
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
