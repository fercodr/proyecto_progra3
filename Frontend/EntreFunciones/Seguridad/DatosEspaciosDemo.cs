namespace EntreFunciones.Seguridad {
    // Catalogos fijos de opciones validas para los formularios de Salas/Butacas (no son datos
    // simulados de negocio: las listas reales de Sedes/Salas/Butacas vienen de ISedeService/
    // ISalaService/IButacaService).
    public static class DatosEspaciosDemo {
        public static readonly string[] TiposSala = ["2D", "3D", "IMAX", "Teatro", "Auditorio"];
        public static readonly string[] TiposButaca = ["General", "Preferencial", "Accesible"];
    }
}
