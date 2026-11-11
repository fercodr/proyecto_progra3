using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class AdministradorDTO : Dto
    {
        public string Nombres { get; set; } = string.Empty;
        public string Apellidos { get; set; } = string.Empty;

        [JsonIgnore]
        public bool Activo
        {
            get => IsActivo;
            set => IsActivo = value;
        }
    }
}
