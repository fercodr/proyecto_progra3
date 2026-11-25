using System.Text.Json;
using System.Text.Json.Serialization;

namespace EntreFunciones.Servicios.Rest;

/// <summary>
/// El backend espera java.time.LocalDateTime (sin zona horaria). DateTime en .NET
/// puede tener Kind=Local (por ejemplo, viene de DateTime.Now), y el serializador
/// por defecto de System.Text.Json en ese caso agrega el offset de la zona horaria
/// (ej. "-05:00") al final del string. Yasson/JSON-B del lado de Java no acepta ese
/// offset en un LocalDateTime y la peticion falla con un error de deserializacion.
/// Este conversor fuerza el formato sin offset, sin importar el Kind del DateTime.
/// </summary>
public sealed class LocalDateTimeJsonConverter : JsonConverter<DateTime>
{
    private const string Formato = "yyyy-MM-ddTHH:mm:ss.fffffff";

    public override DateTime Read(
        ref Utf8JsonReader reader,
        Type typeToConvert,
        JsonSerializerOptions options)
    {
        string? texto = reader.GetString();
        return string.IsNullOrWhiteSpace(texto)
            ? default
            : DateTime.Parse(texto, System.Globalization.CultureInfo.InvariantCulture);
    }

    public override void Write(
        Utf8JsonWriter writer,
        DateTime value,
        JsonSerializerOptions options)
    {
        writer.WriteStringValue(value.ToString(
            Formato, System.Globalization.CultureInfo.InvariantCulture));
    }
}
