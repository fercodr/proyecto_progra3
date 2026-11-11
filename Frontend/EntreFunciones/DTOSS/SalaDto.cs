namespace EntreFunciones.DTOSS
{
    public class SalaDto : Dto
    {
        public int IdSede { get; set; }
        public SedeDto? Sede { get; set; }
        public string Nombre { get; set; } = string.Empty;
        public int Aforo { get; set; }
        public string TipoSala { get; set; } = string.Empty;
    }
}
