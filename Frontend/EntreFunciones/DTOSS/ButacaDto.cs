using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class ButacaDto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("fila")]
        public string Fila { get; set; } = string.Empty;

        [JsonPropertyName("columna")]
        public int Columna { get; set; }

        [JsonPropertyName("activo")]
        public bool Activo { get; set; }

        [JsonPropertyName("ocupada")]
        public bool Ocupada { get; set; }

        public bool Elegida { get; set; }
    }
}
