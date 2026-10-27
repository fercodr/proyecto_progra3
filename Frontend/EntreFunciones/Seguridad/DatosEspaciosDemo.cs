using EntreFunciones.ViewModels;

namespace EntreFunciones.Seguridad {
    // Datos hardcodeados para maquetar las pantallas de Espacios sin conectar a la BD ni al BL (Lab 9).
    // Cuando se integren los servicios web, las páginas llamarán al servicio en lugar de usar esta clase.
    public static class DatosEspaciosDemo {
        public static readonly string[] TiposSala = ["2D", "3D", "IMAX", "Teatro", "Auditorio"];
        public static readonly string[] TiposButaca = ["General", "Preferencial", "Accesible"];
        public static readonly string[] TiposRecurso = ["Proyección", "Audio", "Iluminación", "Escenografía", "Accesibilidad"];

        public static readonly List<SedeViewModel> Sedes = [
            new() { Id = 1, RazonSocial = "EntreFunciones Miraflores", Direccion = "Av. Larco 1150", Ciudad = "Lima", TelefonoContacto = "014451200" },
            new() { Id = 2, RazonSocial = "EntreFunciones San Miguel", Direccion = "Av. La Marina 2000", Ciudad = "Lima", TelefonoContacto = "015783300" },
            new() { Id = 3, RazonSocial = "EntreFunciones Arequipa Centro", Direccion = "Calle Mercaderes 212", Ciudad = "Arequipa", TelefonoContacto = "054228800" },
            new() { Id = 4, RazonSocial = "EntreFunciones Surco", Direccion = "Av. Primavera 645", Ciudad = "Lima", TelefonoContacto = "013449010", IsActivo = false }
        ];

        public static readonly List<SalaViewModel> Salas = [
            new() { Id = 1, IdSede = 1, NombreSede = "EntreFunciones Miraflores", Nombre = "Sala 1", Aforo = 96, TipoSala = "2D" },
            new() { Id = 2, IdSede = 1, NombreSede = "EntreFunciones Miraflores", Nombre = "Sala 2", Aforo = 120, TipoSala = "3D" },
            new() { Id = 3, IdSede = 1, NombreSede = "EntreFunciones Miraflores", Nombre = "Sala IMAX", Aforo = 220, TipoSala = "IMAX" },
            new() { Id = 4, IdSede = 1, NombreSede = "EntreFunciones Miraflores", Nombre = "Teatro Larco", Aforo = 300, TipoSala = "Teatro" },
            new() { Id = 5, IdSede = 2, NombreSede = "EntreFunciones San Miguel", Nombre = "Sala 1", Aforo = 110, TipoSala = "2D" },
            new() { Id = 6, IdSede = 2, NombreSede = "EntreFunciones San Miguel", Nombre = "Sala 2", Aforo = 80, TipoSala = "2D", IsActivo = false },
            new() { Id = 7, IdSede = 3, NombreSede = "EntreFunciones Arequipa Centro", Nombre = "Auditorio Mercaderes", Aforo = 180, TipoSala = "Auditorio" }
        ];

        public static readonly List<RecursoViewModel> Recursos = [
            new() { Id = 1, Nombre = "Proyector láser 4K #1", Tipo = "Proyección" },
            new() { Id = 2, Nombre = "Proyector láser 4K #2", Tipo = "Proyección" },
            new() { Id = 3, Nombre = "Sistema de sonido Dolby Atmos", Tipo = "Audio" },
            new() { Id = 4, Nombre = "Micrófonos inalámbricos (set de 8)", Tipo = "Audio" },
            new() { Id = 5, Nombre = "Consola de iluminación", Tipo = "Iluminación" },
            new() { Id = 6, Nombre = "Escenario modular 6 x 4 m", Tipo = "Escenografía" },
            new() { Id = 7, Nombre = "Rampa de accesibilidad portátil", Tipo = "Accesibilidad", IsActivo = false }
        ];

        // Fechas relativas a hoy para que siempre haya bloqueos vigentes, programados y finalizados
        public static readonly List<BloqueoViewModel> Bloqueos = [
            new() { Id = 1, TipoObjetivo = "Sala", IdSala = 2, NombreObjetivo = "Sala 2 · Miraflores", Motivo = "Cambio de pantalla",
                    FechaInicio = DateTime.Today.AddDays(-1).AddHours(8), FechaFin = DateTime.Today.AddDays(2).AddHours(18) },
            new() { Id = 2, TipoObjetivo = "Recurso", IdRecurso = 2, NombreObjetivo = "Proyector láser 4K #2", Motivo = "Mantenimiento preventivo",
                    FechaInicio = DateTime.Today.AddHours(9), FechaFin = DateTime.Today.AddDays(1).AddHours(13) },
            new() { Id = 3, TipoObjetivo = "Sala", IdSala = 4, NombreObjetivo = "Teatro Larco · Miraflores", Motivo = "Evento privado",
                    FechaInicio = DateTime.Today.AddDays(6).AddHours(15), FechaFin = DateTime.Today.AddDays(6).AddHours(23) },
            new() { Id = 4, TipoObjetivo = "Recurso", IdRecurso = 5, NombreObjetivo = "Consola de iluminación", Motivo = "Reparación de canal DMX",
                    FechaInicio = DateTime.Today.AddDays(-12).AddHours(10), FechaFin = DateTime.Today.AddDays(-9).AddHours(18) }
        ];

        // Mapa de ejemplo de una sala: 8 filas (A-H) x 12 columnas = 96 butacas
        public static List<ButacaViewModel> ButacasDeSala(int idSala) {
            List<ButacaViewModel> butacas = [];
            int id = 1;
            foreach (char fila in "ABCDEFGH") {
                for (int columna = 1; columna <= 12; columna++) {
                    ButacaViewModel butaca = new() { Id = id++, IdSala = idSala, Fila = fila.ToString(), Columna = columna };
                    if (fila == 'G' || fila == 'H') {
                        butaca.TipoButaca = "Preferencial";
                    }
                    if (fila == 'A' && columna <= 2) {
                        butaca.TipoButaca = "Accesible";
                    }
                    if (columna == 12 && (fila == 'D' || fila == 'E')) {
                        butaca.IsActivo = false;
                    }
                    butacas.Add(butaca);
                }
            }
            return butacas;
        }
    }
}
