namespace EntreFunciones.DTOSS
{
    public class AdministradorDTO : Dto
    {
        public string Nombres { get; set; } = string.Empty;
        public string Apellidos { get; set; } = string.Empty;
        public bool Activo { get; set; }
    }
}
