package pe.edu.pucp.entrefunciones.rest;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.SecurityContext;

public final class AutorizacionRest {
    private AutorizacionRest() {
    }

    public static int usuarioId(SecurityContext sesion) {
        if (sesion == null || sesion.getUserPrincipal() == null) {
            throw new ForbiddenException("Se requiere una sesion autenticada");
        }
        try {
            int id = Integer.parseInt(sesion.getUserPrincipal().getName());
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new ForbiddenException("La identidad de la sesion no es valida");
        }
    }

    public static boolean esAdministrador(SecurityContext sesion) {
        return sesion != null && sesion.isUserInRole("Administrador");
    }

    public static void exigirAdministrador(SecurityContext sesion) {
        if (!esAdministrador(sesion)) {
            throw new ForbiddenException("Se requiere administrador");
        }
    }

    public static void exigirCliente(SecurityContext sesion) {
        if (sesion == null || !sesion.isUserInRole("Cliente")) {
            throw new ForbiddenException("Se requiere una cuenta cliente");
        }
    }

    public static void exigirPropietarioOAdministrador(
            SecurityContext sesion, int propietarioId) {
        if (!esAdministrador(sesion) && usuarioId(sesion) != propietarioId) {
            throw new ForbiddenException("No puede consultar o modificar datos de otra cuenta");
        }
    }
}
