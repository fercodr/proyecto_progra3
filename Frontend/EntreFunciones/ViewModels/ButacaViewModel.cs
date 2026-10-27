using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels {
    public class ButacaViewModel {
        public int Id { get; set; }

        public int IdSala { get; set; }

        [Required(ErrorMessage = "La fila es obligatoria")]
        [RegularExpression(@"^[A-Z]{1,2}$", ErrorMessage = "Use letras mayúsculas (A, B, ... AA)")]
        public string Fila { get; set; } = "";

        [Range(1, 50, ErrorMessage = "La columna debe estar entre 1 y 50")]
        public int Columna { get; set; } = 1;

        [Required(ErrorMessage = "Seleccione el tipo de butaca")]
        public string TipoButaca { get; set; } = "General";

        public bool IsActivo { get; set; } = true;

        public string Codigo => $"{Fila}{Columna}";

        public ButacaViewModel Clonar() => (ButacaViewModel)MemberwiseClone();
    }
}
