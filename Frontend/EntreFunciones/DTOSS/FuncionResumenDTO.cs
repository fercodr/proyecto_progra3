using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{

    public class FuncionResumenDTO : Dto
    {
        [JsonPropertyName("tituloEvento")]
        public string Titulo { get; set; } = string.Empty;
        [JsonPropertyName("fecha")]
        public DateOnly Fecha { get; set; }
        [JsonPropertyName("horaInicio")]
        public TimeOnly HoraInicio { get; set; }
        [JsonPropertyName("nombreSede")]
        public string Sede { get; set; } = string.Empty;
        [JsonPropertyName("nombreSala")]
        public string Sala { get; set; } = string.Empty;
        [JsonPropertyName("estado")]
        public string Estado { get; set; } = string.Empty;
    }
}
