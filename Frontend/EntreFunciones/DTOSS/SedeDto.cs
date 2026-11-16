using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class SedeDto : Dto
    {
        [JsonPropertyName("razonSocial")]
        public string RazonSocial { get; set; } = string.Empty;

        [JsonPropertyName("direccion")]
        public string Direccion { get; set; } = string.Empty;

        [JsonPropertyName("ciudad")]
        public string Ciudad { get; set; } = string.Empty;

        [JsonPropertyName("distrito")]
        public string Distrito { get; set; } = string.Empty;

        [JsonPropertyName("telefonoContacto")]
        public string? TelefonoContacto { get; set; }

        // Solo viene con ?completo=true
        [JsonPropertyName("salas")]
        public List<SalaDto>? Salas { get; set; }
    }
}
