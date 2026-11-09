using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class ReservaDto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("codigoOId")]
        public string CodigoOId { get; set; } = string.Empty;

        [JsonPropertyName("funcionId")]
        public int FuncionId { get; set; }

        [JsonPropertyName("funcionNombre")]
        public string FuncionNombre { get; set; } = string.Empty;

        [JsonPropertyName("fechaFuncion")]
        public DateTime FechaFuncion { get; set; }

        [JsonPropertyName("cliente")]
        public string Cliente { get; set; } = string.Empty;

        [JsonPropertyName("dni")]
        public string Dni { get; set; } = string.Empty;

        [JsonPropertyName("cantidad")]
        public int Cantidad { get; set; }

        [JsonPropertyName("fechaCreacion")]
        public DateTime FechaCreacion { get; set; }

        [JsonPropertyName("fechaExpiracion")]
        public DateTime FechaExpiracion { get; set; }

        [JsonPropertyName("fechaServidor")]
        public DateTime FechaServidor { get; set; }

        [JsonPropertyName("montoTotal")]
        public decimal MontoTotal { get; set; }

        [JsonPropertyName("estado")]
        public string Estado { get; set; } = string.Empty;
    }
}
