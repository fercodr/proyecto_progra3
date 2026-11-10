namespace EntreFunciones.ViewModels
{
    public class AdministradorViewModel : ViewModel
    {
        public string Nombres { get; set; } = string.Empty;
        public string Apellidos { get; set; } = string.Empty;

        public string NombreCompleto => $"{Nombres} {Apellidos}".Trim();
    }
}
