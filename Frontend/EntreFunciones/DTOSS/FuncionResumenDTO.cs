namespace EntreFunciones.DTOSS
{

    public class FuncionResumenDTO : Dto
    {
        public string Titulo { get; set; } = string.Empty;
        public DateOnly Fecha { get; set; }
        public TimeOnly HoraInicio { get; set; }
        public string Sede { get; set; } = string.Empty;
        public string Sala { get; set; } = string.Empty;
        public string Estado { get; set; } = string.Empty;
    }
}
