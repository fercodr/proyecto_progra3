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
            ["Espacios"] = ["Administrador"],  // 11 Sedes y salas, 12 Butacas, 13 Recursos y bloqueos
            ["Programacion"] = ["Administrador"],  // 14 Catálogo, 15 Funciones, 16 Programar función
            ["Ventas"] = ["Administrador"],  // 17 Ventas, 18 Validación de ingreso
            ["Incidencias"] = ["Administrador"],  // 19 Incidencias
            ["UsuariosRoles"] = ["Administrador"]   // 20 Usuarios y roles
        };

        // Grupo del menú -> opciones. Solo se muestran las que el rol del usuario permite.
        // Las líneas comentadas se activan cuando exista la página correspondiente (si no, el enlace da "no encontrado").
        public static readonly IReadOnlyDictionary<string, List<OpcionMenu>> Menu = new Dictionary<string, List<OpcionMenu>>
        {
            // ["Mis compras"] = [
            //     new OpcionMenu { Texto = "Mis compras", Href = "mis-compras", Permiso = "Compras" }
            // ],
            // ["Reportes"] = [
            //     new OpcionMenu { Texto = "Reportes", Href = "admin", Permiso = "Reportes" }
            // ],
            // ["Espacios"] = [
            //     new OpcionMenu { Texto = "Sedes y salas", Href = "admin/sedes", Permiso = "Espacios" },
            //     new OpcionMenu { Texto = "Recursos y bloqueos", Href = "admin/recursos", Permiso = "Espacios" }
            // ],
            // ["Programación"] = [
            //     new OpcionMenu { Texto = "Catálogo", Href = "admin/catalogo", Permiso = "Programacion" },
            //     new OpcionMenu { Texto = "Funciones", Href = "admin/funciones", Permiso = "Programacion" }
            // ],
            // ["Ventas"] = [
            //     new OpcionMenu { Texto = "Ventas", Href = "admin/ventas", Permiso = "Ventas" },
            //     new OpcionMenu { Texto = "Validación de ingreso", Href = "admin/validar-ingreso", Permiso = "Ventas" }
            // ],
            ["Control"] = [
                // new OpcionMenu { Texto = "Incidencias", Href = "admin/incidencias", Permiso = "Incidencias" },
                new OpcionMenu { Texto = "Usuarios y roles", Href = "admin/usuarios", Permiso = "UsuariosRoles" }
            ]
        };

        // Opciones del menú desplegable del nombre de usuario.
        public static readonly List<OpcionMenu> MenuCuenta = [
            // new OpcionMenu { Texto = "Mi perfil", Href = "perfil", Permiso = "PerfilCliente" }
        ];

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

        public static List<OpcionMenu> OpcionesDelUsuario(List<OpcionMenu> opciones, ClaimsPrincipal usuario)
        {
            List<OpcionMenu> opcionesPermitidas = [];
            foreach (OpcionMenu opcion in opciones)
            {
                if (TienePermiso(usuario, opcion.Permiso))
                {
                    opcionesPermitidas.Add(opcion);
                }
            }
            return opcionesPermitidas;
        }

        public static Dictionary<string, List<OpcionMenu>> MenuDelUsuario(ClaimsPrincipal usuario)
        {
            Dictionary<string, List<OpcionMenu>> menu = [];
            foreach (KeyValuePair<string, List<OpcionMenu>> grupo in Menu)
            {
                List<OpcionMenu> opciones = OpcionesDelUsuario(grupo.Value, usuario);
                if (opciones.Count > 0)
                {
                    menu[grupo.Key] = opciones;
                }
            }
            return menu;
        }
    }
}
