using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class CrearReservaRequestDto
    {
        [JsonPropertyName("funcionId")]
        public int FuncionId { get; set; }

        [JsonPropertyName("butacaIds")]
        public List<int> ButacaIds { get; set; } = [];

        [JsonPropertyName("cantidad")]
        public int Cantidad { get; set; }
    }
}
