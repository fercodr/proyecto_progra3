using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class PagoDto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("reservaId")]
        public int ReservaId { get; set; }

        [JsonPropertyName("codigoPago")]
        public string CodigoPago { get; set; } = string.Empty;

        [JsonPropertyName("cliente")]
        public string Cliente { get; set; } = string.Empty;

        [JsonPropertyName("monto")]
        public double Monto { get; set; }

        [JsonPropertyName("fechaPago")]
        public string FechaPago { get; set; } = string.Empty;

        [JsonPropertyName("estado")]
        public string Estado { get; set; } = string.Empty;
    }
}
