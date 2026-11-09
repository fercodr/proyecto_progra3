namespace EntreFunciones.DTOSS
{
    public class AuditoriaDTO : Dto
    {
        public int FuncionId { get; set; }
        public DateTime? Fecha { get; set; }
        public string Responsable { get; set; } = string.Empty;
        public string Descripcion { get; set; } = string.Empty;
        public EstadoAuditoria Estado { get; set; }
    }
}
