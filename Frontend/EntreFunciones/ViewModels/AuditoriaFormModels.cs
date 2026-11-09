using EntreFunciones.DTOSS;
using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels
{
    public class NuevaAuditoriaFormModel
    {
        [Range(1, int.MaxValue, ErrorMessage = "Seleccione una funcion.")]
        public int FuncionId { get; set; }

        [Range(1, int.MaxValue, ErrorMessage = "Seleccione un responsable.")]
        public int ResponsableId { get; set; }

        [Required(ErrorMessage = "Ingrese la descripcion.")]
        [StringLength(500, ErrorMessage =
            "La descripcion no puede superar 500 caracteres.")]
        public string Descripcion { get; set; } = string.Empty;
    }

    public class AtencionAuditoriaFormModel
    {
        [Range(1, int.MaxValue, ErrorMessage = "Seleccione un responsable.")]
        public int ResponsableId { get; set; }

        public EstadoAuditoria Estado { get; set; }
    }
}
