using EntreFunciones.ViewModels;

namespace EntreFunciones.Seguridad {
    // Datos hardcodeados para maquetar la pantalla de Ventas sin conectar a la BD ni al BL (Lab 9).
    // Mismos valores de ejemplo que ya estaban en Ventas.razor, solo movidos aqui para poder filtrar/enlistar
    // con el mismo patron que las demas pantallas (ver DatosEspaciosDemo.cs).
    public static class DatosVentasDemo {
        public static readonly string[] Funciones = [
            "Evento A · 28/10, 18:00",
            "Evento B · 28/10, 19:00",
            "Evento C · 29/10, 17:00"
        ];

        public static readonly string[] EstadosReserva = ["Pendiente", "Confirmada", "Expirada", "Cancelada"];

        public static readonly List<ReservaVentaViewModel> Reservas = [
            new() { Codigo = "RES-001", Cliente = "Ana Torres", Dni = "12345678", Funcion = "Evento A", FechaFuncion = "28/10/2026 · 18:00", Cantidad = 2, Expira = "27/10/2026 · 16:30", Estado = "Pendiente" },
            new() { Codigo = "RES-002", Cliente = "Luis Pérez", Dni = "23456789", Funcion = "Evento B", FechaFuncion = "28/10/2026 · 19:00", Cantidad = 1, Expira = "27/10/2026 · 15:30", Estado = "Confirmada" },
            new() { Codigo = "RES-003", Cliente = "María Soto", Dni = "34567890", Funcion = "Evento C", FechaFuncion = "29/10/2026 · 17:00", Cantidad = 3, Expira = "26/10/2026 · 16:00", Estado = "Expirada" }
        ];

        public static readonly List<PagoVentaViewModel> Pagos = [
            new() { Codigo = "PAG-001", Reserva = "RES-001", Cliente = "Ana Torres", Monto = 60.00, FechaPago = "27/10/2026 · 16:00", Estado = "Pendiente" },
            new() { Codigo = "PAG-002", Reserva = "RES-002", Cliente = "Luis Pérez", Monto = 30.00, FechaPago = "27/10/2026 · 15:00", Estado = "Validado" },
            new() { Codigo = "PAG-003", Reserva = "RES-003", Cliente = "María Soto", Monto = 90.00, FechaPago = "26/10/2026 · 15:50", Estado = "Rechazado" }
        ];
    }
}
