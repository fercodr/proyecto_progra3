namespace EntreFunciones.DTOSS 
{
    public class AuditoriaDTO:Dto
    {
        private FuncionDTO Funcion { get; set; }
        private DateTime Fecha { get; set; }
        private String responsable { get; set; }
        private String descripcion { get; set; }
        private EstadoAuditoria Estado { get; set; }
    }
}
