using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels {
    public class RecursoViewModel {
        public int Id { get; set; }

        [Required(ErrorMessage = "El nombre es obligatorio")]
        [StringLength(80, ErrorMessage = "Máximo 80 caracteres")]
        public string Nombre { get; set; } = "";

        [Required(ErrorMessage = "Seleccione el tipo de recurso")]
        public string Tipo { get; set; } = "";

        public bool IsActivo { get; set; } = true;

        public RecursoViewModel Clonar() => (RecursoViewModel)MemberwiseClone();
    }
}
