using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    [JsonConverter(typeof(JsonStringEnumConverter))]
    public enum EstadoAuditoria
    {
        PENDIENTE,
        EN_PROCESO,
        RESUELTA,
    }
}
