using System.Text.Json.Serialization;

namespace EntreFunciones.DTOSS.Usuarios
{
    // lo que responde POST  (sin contraseña)
    public class CuentaUsuarioDTO
    {
     
        public int Id { get; set; }
        public bool Activo { get; set; }
        public string Correo { get; set; } = string.Empty;
    }
}
