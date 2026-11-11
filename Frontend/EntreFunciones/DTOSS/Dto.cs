using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public abstract class Dto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("activo")]
        public bool IsActivo { get; set; } = true;
    }
}
