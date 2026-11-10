using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS.Usuarios
{
    // Gemelo de ClienteDTO.java y se llena al ENVIAR (registro).
    public class ClienteDTO
    {
 
        public int Id { get; set; }
        public bool Activo { get; set; } = true;
        public string Nombres { get; set; } = string.Empty;
        public string Apellidos { get; set; } = string.Empty;
        public string Correo { get; set; } = string.Empty;
        public string? Contrasena { get; set; }
        public string Dni { get; set; } = string.Empty;
        public string Telefono { get; set; } = string.Empty;
        public string? FechaRegistro { get; set; }
    }
}
