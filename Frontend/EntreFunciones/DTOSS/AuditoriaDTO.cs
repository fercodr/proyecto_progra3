namespace EntreFunciones.DTOSS
{
    public class AuditoriaDTO : Dto
    {
        public FuncionDTO? Funcion { get; set; }
        public DateTime Fecha { get; set; }
        public AdministradorDTO? Responsable { get; set; }
        public string? ResponsableTextoOriginal { get; set; }
        public string Descripcion { get; set; } = string.Empty;
        public EstadoAuditoria Estado { get; set; }
    }
}
