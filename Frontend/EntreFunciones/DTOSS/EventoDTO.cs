using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    // No hereda de Dto: Dto.Id/IsActivo no tienen [JsonPropertyName] y Jackson (Java) serializa en
    // minuscula ("id", "activo"), asi que System.Text.Json (case-sensitive por defecto, ver HttpUtils)
    // no los mapearia. Mismo patron que ya usa ButacaDto.cs.
    public class EventoDTO
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("activo")]
        public bool Activo { get; set; } = true;

        // "PELICULA" | "OBRA_TEATRO" | "ACTIVIDAD_CULTURAL"
        [JsonPropertyName("tipo")]
        public string Tipo { get; set; } = string.Empty;

        [JsonPropertyName("precio")]
        public double Precio { get; set; }

        [JsonPropertyName("titulo")]
        public string Titulo { get; set; } = string.Empty;

        [JsonPropertyName("imagenUrl")]
        public string? ImagenUrl { get; set; }

        [JsonPropertyName("requiereAsientoNumerado")]
        public bool RequiereAsientoNumerado { get; set; }

        // Pelicula
        [JsonPropertyName("duracionMinutos")]
        public int? DuracionMinutos { get; set; }

        [JsonPropertyName("genero")]
        public string? Genero { get; set; }

        [JsonPropertyName("clasificacionPorEdad")]
        public string? ClasificacionPorEdad { get; set; }

        [JsonPropertyName("director")]
        public string? Director { get; set; }

        // ObraTeatro
        [JsonPropertyName("nombreDirector")]
        public string? NombreDirector { get; set; }

        [JsonPropertyName("elenco")]
        public string? Elenco { get; set; }

        // ActividadCultural
        [JsonPropertyName("expositor")]
        public string? Expositor { get; set; }

        [JsonPropertyName("tipoActividad")]
        public string? TipoActividad { get; set; }

        [JsonPropertyName("funciones")]
        public List<FuncionDTO>? Funciones { get; set; }
    }
}
