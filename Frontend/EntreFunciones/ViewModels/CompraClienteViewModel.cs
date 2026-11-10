namespace EntreFunciones.ViewModels
{
    // Fila de "Mis compras": una reserva del cliente con su estado ya traducido para mostrar.
    public class CompraClienteViewModel : ViewModel
    {
        public string Codigo { get; set; } = "";
        public string Funcion { get; set; } = "";
        public DateTime FechaFuncion { get; set; }
        public int Cantidad { get; set; }
        public decimal Monto { get; set; }
        public DateTime FechaCreacion { get; set; }
        public DateTime FechaExpiracion { get; set; }
        /// <summary>Texto mostrado al usuario (Pendiente de pago, Confirmada, Expirada, Cancelada).</summary>
        public string Estado { get; set; } = "";
        /// <summary>Clase de Bootstrap del badge del estado.</summary>
        public string EstadoClase { get; set; } = "";
        /// <summary>La reserva sigue vigente y todavía se puede pagar o cancelar.</summary>
        public bool Pendiente { get; set; }
    }
}
