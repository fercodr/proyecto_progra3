using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels {
    public class BloqueoViewModel : IValidatableObject {
        public int Id { get; set; }

        // "Sala" o "Recurso": un bloqueo se aplica a una sala o a un recurso
        [Required(ErrorMessage = "Indique qué desea bloquear")]
        public string TipoObjetivo { get; set; } = "Sala";

        public int? IdSala { get; set; }

        public int? IdRecurso { get; set; }

        // Solo para mostrar en el listado
        public string NombreObjetivo { get; set; } = "";

        [Required(ErrorMessage = "El motivo es obligatorio")]
        [StringLength(200, ErrorMessage = "Máximo 200 caracteres")]
        public string Motivo { get; set; } = "";

        public DateTime FechaInicio { get; set; } = DateTime.Today.AddHours(8);

        public DateTime FechaFin { get; set; } = DateTime.Today.AddHours(18);

        public bool IsActivo { get; set; } = true;

        public string Estado {
            get {
                if (DateTime.Now < FechaInicio) {
                    return "Programado";
                }
                if (DateTime.Now > FechaFin) {
                    return "Finalizado";
                }
                return "Vigente";
            }
        }

        public IEnumerable<ValidationResult> Validate(ValidationContext validationContext) {
            if (FechaFin <= FechaInicio) {
                yield return new ValidationResult("La fecha de fin debe ser posterior a la de inicio", [nameof(FechaFin)]);
            }
            if (TipoObjetivo == "Sala" && IdSala == null) {
                yield return new ValidationResult("Seleccione la sala", [nameof(IdSala)]);
            }
            if (TipoObjetivo == "Recurso" && IdRecurso == null) {
                yield return new ValidationResult("Seleccione el recurso", [nameof(IdRecurso)]);
            }
        }

        public BloqueoViewModel Clonar() => (BloqueoViewModel)MemberwiseClone();
    }
}
