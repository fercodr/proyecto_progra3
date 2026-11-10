using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public abstract class Dto
    {
        public int Id { get; set; }

        [JsonPropertyName("activo")]
        public bool IsActivo { get; set; } = true;
    }
}
