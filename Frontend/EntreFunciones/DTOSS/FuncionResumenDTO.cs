namespace EntreFunciones.DTOSS
{
    // Resumen de función que devuelve /auditorias (ver FuncionResumenDTO.java).
    // Antes se llamaba FuncionDTO, pero chocaba con el FuncionDTO del Catálogo en el merge.
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
