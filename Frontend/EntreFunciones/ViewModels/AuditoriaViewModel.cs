using EntreFunciones.DTOSS;
using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels
{
    public class AuditoriaViewModel : ViewModel
    {
        [Range(1, int.MaxValue, ErrorMessage = "Seleccione una función.")]
        public int FuncionId { get; set; }

        [Required(ErrorMessage = "Ingrese la descripción.")]
        public string Descripcion { get; set; } = string.Empty;

        [Required(ErrorMessage = "Indique el responsable.")]
        public string Responsable { get; set; } = string.Empty;

        [Required(ErrorMessage = "Indique la fecha.")]
        public DateTime? Fecha { get; set; }

        [Required(ErrorMessage = "Indique un estado.")]
        public EstadoAuditoria EstadoAuditoria { get; set; }
    }
}
