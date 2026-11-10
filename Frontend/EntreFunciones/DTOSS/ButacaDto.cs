using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS
{
    public class ButacaDto : Dto
    {
        public int IdSala { get; set; }
        public string Fila { get; set; } = string.Empty;
        public int Columna { get; set; }
        public string TipoButaca { get; set; } = string.Empty;
        
        // Solo en el flujo de compra (disponibilidad de una función)
        [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingDefault)]
        public bool Ocupada { get; set; }

        // Solo en el cliente: no viene del backend
        [JsonIgnore]
        public bool Elegida { get; set; }

        // Alias para el código de compra existente (.Activo)
        [JsonIgnore]
        public bool Activo
        {
            get => IsActivo;
            set => IsActivo = value;
        }
    }
}
