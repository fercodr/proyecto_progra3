using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels {
    public class SalaViewModel {
        public int Id { get; set; }

        [Range(1, int.MaxValue, ErrorMessage = "Seleccione una sede")]
        public int IdSede { get; set; }

        // Solo para mostrar en el listado
        public string NombreSede { get; set; } = "";

        [Required(ErrorMessage = "El nombre es obligatorio")]
        [StringLength(50, ErrorMessage = "Máximo 50 caracteres")]
        public string Nombre { get; set; } = "";

        [Range(1, 1000, ErrorMessage = "El aforo debe estar entre 1 y 1000")]
        public int Aforo { get; set; }

        [Required(ErrorMessage = "Seleccione el tipo de sala")]
        public string TipoSala { get; set; } = "";

        public bool IsActivo { get; set; } = true;

        public SalaViewModel Clonar() => (SalaViewModel)MemberwiseClone();
    }
}
