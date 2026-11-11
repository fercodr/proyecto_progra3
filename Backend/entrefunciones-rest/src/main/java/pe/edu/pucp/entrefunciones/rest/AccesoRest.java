package pe.edu.pucp.entrefunciones.rest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.rest.auth.JwtServicio;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AccesoRest implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) {
        if (esAccesoPublico(request)) {
            return;
        }

        String cabecera = request.getHeaderString("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            rechazar(request, 401, "Se requiere un token Bearer");
            return;
        }

        String token = cabecera.substring("Bearer ".length()).trim();
        try {
            Claims claims = JwtServicio.validar(token);
            autenticarCuenta(request, claims);
        } catch (JwtException | IllegalArgumentException ex) {
            rechazar(request, 401, "El token no es valido");
        } catch (IllegalStateException ex) {
            rechazar(request, 503, "Falta configurar la autenticacion REST");
        }
    }

    private void autenticarCuenta(ContainerRequestContext request, Claims claims) {
        int id;
        try {
            id = Integer.parseInt(claims.getSubject());
            if (id <= 0 || JwtServicio.rol(claims) == null) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException ex) {
            rechazar(request, 401, "El token no contiene una identidad valida");
            return;
        }

        String rolToken = JwtServicio.rol(claims);
        try (Connection connection = DBManager.getInstance().getConnection();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT EXISTS(SELECT 1 FROM administrador a "
                             + "WHERE a.usuario_id = u.id) AS admin "
                             + "FROM usuario u WHERE u.id = ? AND u.activo")) {
            query.setInt(1, id);
            try (ResultSet resultSet = query.executeQuery()) {
                if (!resultSet.next()) {
                    rechazar(request, 401, "La cuenta no esta activa");
                    return;
                }
                String rolReal = resultSet.getBoolean("admin")
                        ? "Administrador"
                        : "Cliente";
                if (!rolReal.equals(rolToken)) {
                    rechazar(request, 401, "La identidad del token no es valida");
                    return;
                }
                SecurityContext anterior = request.getSecurityContext();
                request.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> Integer.toString(id);
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return rolReal.equals(role);
                    }

                    @Override
                    public boolean isSecure() {
                        return anterior != null && anterior.isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "Bearer";
                    }
                });
            }
        } catch (java.sql.SQLException ex) {
            rechazar(request, 503, "No se pudo verificar la cuenta");
        }
    }

    private boolean esAccesoPublico(ContainerRequestContext request) {
        String ruta = request.getUriInfo().getPath().replaceFirst("^/", "");
        if ("POST".equalsIgnoreCase(request.getMethod())
                && "cuentas/login".equals(ruta)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(request.getMethod())
                && ("cuentas/recuperar".equals(ruta)
                    || "clientes".equals(ruta))) {
            return true;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return "eventos".equals(ruta)
                || ruta.startsWith("eventos/")
                || "funciones".equals(ruta)
                || (ruta.startsWith("funciones/")
                    && !ruta.equals("funciones/reportes")
                    && !ruta.endsWith("/disponibilidad"));
    }

    private void rechazar(ContainerRequestContext request,
                           int status,
                           String mensaje) {
        request.abortWith(Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(java.util.Map.of("mensaje", mensaje))
                .build());
    }
}
