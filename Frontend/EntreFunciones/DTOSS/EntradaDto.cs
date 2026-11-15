using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class EntradaDto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("fechaEmision")]
        public DateTime FechaEmision { get; set; }

        [JsonPropertyName("codigoQR")]
        public string CodigoQR { get; set; } = string.Empty;

        [JsonPropertyName("precioFinal")]
        public double PrecioFinal { get; set; }

        [JsonPropertyName("estado")]
        public string Estado { get; set; } = string.Empty;

        [JsonPropertyName("reservaId")]
        public int ReservaId { get; set; }

        [JsonPropertyName("butacaIdentificador")]
        public string? ButacaIdentificador { get; set; }
    }
}
