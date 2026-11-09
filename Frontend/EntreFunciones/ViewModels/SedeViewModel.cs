using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels {
    public class SedeViewModel :ViewModel{
       

        [Required(ErrorMessage = "La razón social es obligatoria")]
        [StringLength(100, ErrorMessage = "Máximo 100 caracteres")]
        public string RazonSocial { get; set; } = "";

        [Required(ErrorMessage = "La dirección es obligatoria")]
        [StringLength(150, ErrorMessage = "Máximo 150 caracteres")]
        public string Direccion { get; set; } = "";

        [Required(ErrorMessage = "La ciudad es obligatoria")]
        [StringLength(50, ErrorMessage = "Máximo 50 caracteres")]
        public string Ciudad { get; set; } = "";

        [Required(ErrorMessage = "El teléfono es obligatorio")]
        [RegularExpression(@"^[0-9 ]{6,15}$", ErrorMessage = "Solo números (6 a 15 dígitos)")]
        public string TelefonoContacto { get; set; } = "";

        public SedeViewModel Clonar() => (SedeViewModel)MemberwiseClone();
    }
}
