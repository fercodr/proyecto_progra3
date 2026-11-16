using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS;

public class BloqueoSalaDto
{
    [JsonPropertyName("id")] public int Id { get; set; }
    [JsonPropertyName("salaId")] public int SalaId { get; set; }
    [JsonPropertyName("salaNombre")] public string SalaNombre { get; set; } = string.Empty;
    [JsonPropertyName("sedeNombre")] public string SedeNombre { get; set; } = string.Empty;
    [JsonPropertyName("inicio")] public DateTime Inicio { get; set; } = DateTime.Now.AddHours(1);
    [JsonPropertyName("fin")] public DateTime Fin { get; set; } = DateTime.Now.AddHours(2);
    [JsonPropertyName("activo")] public bool Activo { get; set; } = true;
}

public class DisponibilidadProgramacionDto
{
    [JsonPropertyName("disponible")] public bool Disponible { get; set; }
    [JsonPropertyName("motivo")] public string Motivo { get; set; } = string.Empty;
}
