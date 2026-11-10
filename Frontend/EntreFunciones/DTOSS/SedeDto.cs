namespace EntreFunciones.DTOSS
{
    public class SedeDto : Dto
    {
        public string RazonSocial { get; set; } = string.Empty;
        public string Direccion { get; set; } = string.Empty;
        public string Ciudad { get; set; } = string.Empty;
        public string? TelefonoContacto { get; set; }

        // Solo viene con ?completo=true
        public List<SalaDto>? Salas { get; set; }
    }
}
