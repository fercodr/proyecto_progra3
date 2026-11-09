using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class DisponibilidadDto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("titulo")]
        public string Titulo { get; set; } = string.Empty;

        [JsonPropertyName("sede")]
        public string Sede { get; set; } = string.Empty;

        [JsonPropertyName("sala")]
        public string Sala { get; set; } = string.Empty;

        [JsonPropertyName("fecha")]
        public DateTime Fecha { get; set; }

        [JsonPropertyName("horaInicio")]
        public string HoraInicio { get; set; } = string.Empty;

        [JsonPropertyName("precio")]
        public decimal Precio { get; set; }

        [JsonPropertyName("aforoNumerado")]
        public bool AforoNumerado { get; set; }

        [JsonPropertyName("cuposDisponibles")]
        public int CuposDisponibles { get; set; }

        [JsonPropertyName("butacas")]
        public List<ButacaDto> Butacas { get; set; } = [];
    }
}
