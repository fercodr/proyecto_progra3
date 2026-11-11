using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS.Usuarios
{
    public sealed class SesionDTO
    {
        [JsonPropertyName("token")]
        public string Token { get; set; } = string.Empty;

        [JsonPropertyName("vence")]
        public DateTimeOffset Vence { get; set; }

        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("correo")]
        public string Correo { get; set; } = string.Empty;

        [JsonPropertyName("rol")]
        public string Rol { get; set; } = string.Empty;
    }
}
