using System.Security.Claims;

namespace EntreFunciones.Seguridad
{
    public class Permisos
    {
        // Permiso -> roles que pueden entrar. Cada permiso se convierte en una "policy" (ver SeguridadExtensions)
        // y se usa en las páginas así:  @attribute [Authorize(Policy = "NombreDelPermiso")]
        //
        // Roles del sistema (según el documento de pantallas): "Cliente" y "Administrador".
        // Pantallas públicas (01 Login, 02 Registro, 03 Recuperar, 04 Cartelera, 05 Detalle) NO llevan [Authorize].
        public static readonly IReadOnlyDictionary<string, string[]> RolesPorPermiso = new Dictionary<string, string[]>
        {
            // ---- Módulo Cliente ----
            ["Compras"] = ["Cliente"],        // 06 Butacas, 07 Reserva y pago, 08 Mis compras
            ["PerfilCliente"] = ["Cliente"],        // 09 Mi perfil

            // ---- Módulo Administrador ----
            ["Reportes"] = ["Administrador"],  // 10 Reportes (inicio de /admin)
            ["Espacios"] = ["Administrador"],  // 11 Sedes y salas, 12 Butacas
            ["Programacion"] = ["Administrador"],  // 14 Catálogo, 15 Funciones, 16 Programar función
            ["Ventas"] = ["Administrador"],  // 17 Ventas, 18 Validación de ingreso
            ["Incidencias"] = ["Administrador"],  // 19 Incidencias
            ["UsuariosRoles"] = ["Administrador"]   // 20 Usuarios y roles
        };

        private static bool TienePermiso(ClaimsPrincipal usuario, string permiso)
        {
            string[] rolesPermitidos = RolesPorPermiso[permiso];
            foreach (string rol in rolesPermitidos)
            {
                if (usuario.IsInRole(rol))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
