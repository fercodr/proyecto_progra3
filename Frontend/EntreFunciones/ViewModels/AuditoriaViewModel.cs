using EntreFunciones.DTOSS;
using System.ComponentModel.DataAnnotations;

namespace EntreFunciones.ViewModels
{
    public class AuditoriaViewModel : ViewModel
    {
        private FuncionViewModel? funcion;
        private int funcionId;
        private AdministradorViewModel? responsable;
        private int responsableId;

        public FuncionViewModel? Funcion
        {
            get => funcion;
            set
            {
                funcion = value;
                funcionId = value?.Id ?? 0;
            }
        }

        [Range(1, int.MaxValue, ErrorMessage = "Seleccione una funcion.")]
        public int FuncionId
        {
            get => funcionId;
            set
            {
                funcionId = value;
                if (funcion is not null && funcion.Id != value)
                {
                    funcion = null;
                }
            }
        }

        [Required(ErrorMessage = "Ingrese la descripcion.")]
        [StringLength(500, ErrorMessage =
            "La descripcion no puede superar 500 caracteres.")]
        public string Descripcion { get; set; } = string.Empty;

        public AdministradorViewModel? Responsable
        {
            get => responsable;
            set
            {
                responsable = value;
                responsableId = value?.Id ?? 0;
            }
        }

        [Range(1, int.MaxValue, ErrorMessage =
            "Seleccione un responsable.")]
        public int ResponsableId
        {
            get => responsableId;
            set
            {
                responsableId = value;
                if (responsable is not null && responsable.Id != value)
                {
                    responsable = null;
                }
            }
        }

        public string? ResponsableTextoOriginal { get; set; }
        public DateTime Fecha { get; set; }
        public EstadoAuditoria EstadoAuditoria { get; set; }

        public string ResponsablePresentacion => Responsable?.NombreCompleto
            ?? ResponsableTextoOriginal
            ?? "Responsable historico no identificado";
    }
}
