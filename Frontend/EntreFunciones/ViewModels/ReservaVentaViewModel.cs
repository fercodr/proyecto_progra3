namespace EntreFunciones.ViewModels {
    public class ReservaVentaViewModel : ViewModel
    {
        public string Codigo { get; set; } = "";
        public string Cliente { get; set; } = "";
        public string Dni { get; set; } = "";
        public string Funcion { get; set; } = "";
        public string FechaFuncion { get; set; } = "";
        public int Cantidad { get; set; }
        public string Expira { get; set; } = "";
        public string Estado { get; set; } = "";
    }
}
