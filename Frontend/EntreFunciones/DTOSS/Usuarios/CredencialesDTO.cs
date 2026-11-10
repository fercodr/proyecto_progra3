using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS.Usuarios
{
    // Gemelo de CredencialesDTO.java: lo que se envía a POST /cuentas/login
    public class CredencialesDTO
    {
        public string Correo { get; set; } = string.Empty;
        public string Contrasena { get; set; } = string.Empty;
    }
}
