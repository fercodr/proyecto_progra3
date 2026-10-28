namespace EntreFunciones.ViewModels {
    public class PagoVentaViewModel {
        public string Codigo { get; set; } = "";
        public string Reserva { get; set; } = "";
        public string Cliente { get; set; } = "";
        public double Monto { get; set; }
        public string FechaPago { get; set; } = "";
        public string Estado { get; set; } = "";
    }
}
