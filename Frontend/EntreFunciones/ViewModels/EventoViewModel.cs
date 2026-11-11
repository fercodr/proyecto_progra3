namespace EntreFunciones.ViewModels
{
    public class EventoViewModel : ViewModel
    {
        public string Titulo { get; set; } = string.Empty;
        public string Tipo { get; set; } = string.Empty;
        public decimal Precio { get; set; }
    }
}
