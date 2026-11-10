using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS.Usuarios
{
    // solo se llena al ENVIAR (crear administrador).
    public class AdministradorDTO
    {

        public int Id { get; set; }
        public bool Activo { get; set; } = true;
        public string Nombres { get; set; } = string.Empty;
        public string Apellidos { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public string? Contrasena { get; set; }
        public string Dni { get; set; } = string.Empty;
        public string CodigoEmpleado { get; set; } = string.Empty;
    }
}
