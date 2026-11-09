using EntreFunciones.DTOSS;

namespace EntreFunciones.ViewModels
{
    public class AuditoriaViewModel : ViewModel
    {
        public FuncionViewModel? Funcion { get; set; }
        public string Descripcion { get; set; } = string.Empty;
        public AdministradorViewModel? Responsable { get; set; }
        public string? ResponsableTextoOriginal { get; set; }
        public DateTime Fecha { get; set; }
        public EstadoAuditoria EstadoAuditoria { get; set; }

        public string ResponsablePresentacion => Responsable?.NombreCompleto
            ?? ResponsableTextoOriginal
            ?? "Responsable historico no identificado";
    }
}
