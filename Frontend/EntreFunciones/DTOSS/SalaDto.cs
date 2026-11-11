using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class SalaDto : Dto
    {
        [JsonPropertyName("idSede")]
        public int IdSede { get; set; }

        [JsonPropertyName("sede")]
        public SedeDto? Sede { get; set; }

        [JsonPropertyName("nombre")]
        public string Nombre { get; set; } = string.Empty;

        [JsonPropertyName("aforo")]
        public int Aforo { get; set; }

        [JsonPropertyName("tipoSala")]
        public string TipoSala { get; set; } = string.Empty;

        // Solo al registrar una sala nueva con su distribución de butacas
        [JsonPropertyName("butacas")]
        [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
        public List<ButacaDto>? Butacas { get; set; }
    }
}
