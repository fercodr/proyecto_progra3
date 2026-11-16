using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class FuncionDTO
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("idEvento")]
        public int IdEvento { get; set; }

        [JsonPropertyName("tituloEvento")]
        public string? TituloEvento { get; set; }

        [JsonPropertyName("idSala")]
        public int IdSala { get; set; }

        [JsonPropertyName("nombreSala")]
        public string? NombreSala { get; set; }

        [JsonPropertyName("idSede")]
        public int IdSede { get; set; }

        [JsonPropertyName("nombreSede")]
        public string? NombreSede { get; set; }

        [JsonPropertyName("ciudadSede")]
        public string? CiudadSede { get; set; }

        [JsonPropertyName("distritoSede")]
        public string? DistritoSede { get; set; }

        [JsonPropertyName("salaAccesible")]
        public bool SalaAccesible { get; set; }

        // yyyy-MM-dd (ISO, LocalDate.toString() en Java)
        [JsonPropertyName("fecha")]
        public string Fecha { get; set; } = string.Empty;

        // HH:mm o HH:mm:ss (ISO, LocalTime.toString() en Java)
        [JsonPropertyName("horaInicio")]
        public string HoraInicio { get; set; } = string.Empty;

        [JsonPropertyName("horaFin")]
        public string HoraFin { get; set; } = string.Empty;

        [JsonPropertyName("idioma")]
        public string Idioma { get; set; } = string.Empty;

        // PROGRAMADA | CONFIRMADA | CANCELADA
        [JsonPropertyName("estado")]
        public string Estado { get; set; } = string.Empty;

        [JsonPropertyName("evento")]
        public EventoDTO? Evento { get; set; }

        [JsonPropertyName("sala")]
        public SalaDto? Sala { get; set; }

        [JsonPropertyName("sede")]
        public SedeDto? Sede { get; set; }

        [JsonPropertyName("entradasVigentes")]
        public int? EntradasVigentes { get; set; }

        [JsonPropertyName("entradasEmitidas")]
        public int? EntradasEmitidas { get; set; }

        [JsonPropertyName("entradasCanceladas")]
        public int? EntradasCanceladas { get; set; }

        [JsonPropertyName("ingresosConfirmados")]
        public decimal? IngresosConfirmados { get; set; }

        [JsonPropertyName("porcentajeOcupacion")]
        public decimal? PorcentajeOcupacion { get; set; }
    }
}
