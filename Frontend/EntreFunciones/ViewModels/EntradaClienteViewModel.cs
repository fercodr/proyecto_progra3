namespace EntreFunciones.ViewModels
{
    // Fila del historial de entradas de "Mis compras" (RF-M5-03): una entrada emitida,
    // junto con los datos de su reserva y función ya listos para mostrar.
    public class EntradaClienteViewModel : ViewModel
    {
        /// <summary>Identificador visible de la entrada, por ejemplo ENT-000012.</summary>
        public string Codigo { get; set; } = "";
        /// <summary>Texto único que se dibuja como código QR.</summary>
        public string CodigoQR { get; set; } = "";
        public int ReservaId { get; set; }
        public string CodigoReserva { get; set; } = "";
        public string Funcion { get; set; } = "";
        public DateTime FechaFuncion { get; set; }
        /// <summary>Butaca asignada (por ejemplo A5) o "Aforo general" si la función no tiene asientos numerados.</summary>
        public string Butaca { get; set; } = "";
        public decimal Precio { get; set; }
        public DateTime FechaEmision { get; set; }
        /// <summary>Texto mostrado al usuario (Vigente, Cancelada, Utilizada).</summary>
        public string Estado { get; set; } = "";
        /// <summary>Clase de Bootstrap del badge del estado.</summary>
        public string EstadoClase { get; set; } = "";
        /// <summary>La entrada sigue vigente: se puede mostrar su QR para ingresar y todavía se puede cancelar.</summary>
        public bool Vigente { get; set; }
    }
}
