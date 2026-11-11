package pe.edu.pucp.entrefunciones.rest.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class JwtServicio {
    private static final String ALGORITMO = "HS256";
    private static final String ROL = "rol";
    private static final String ISSUER = configuracion("ENTREFUNCIONES_JWT_ISSUER", "entrefunciones-rest");
    private static final String AUDIENCE = configuracion("ENTREFUNCIONES_JWT_AUDIENCE", "entrefunciones-web");
    private static final Duration DURACION = Duration.ofMinutes(
            Long.parseLong(configuracion("ENTREFUNCIONES_JWT_MINUTOS", "480")));
    private static final SecretKey CLAVE = cargarClave();

    private JwtServicio() {
    }

    public static Emision emitir(Usuario usuario) {
        Instant emitido = Instant.now();
        Instant vence = emitido.plus(DURACION);
        String rol = usuario instanceof Administrador
                ? "Administrador"
                : "Cliente";
        String token = Jwts.builder()
                .subject(Integer.toString(usuario.getId()))
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .claim(ROL, rol)
                .issuedAt(Date.from(emitido))
                .expiration(Date.from(vence))
                .signWith(CLAVE, Jwts.SIG.HS256)
                .compact();
        return new Emision(token, vence, usuario.getId(), usuario.getCorreo(), rol);
    }

    public static Claims validar(String token) throws JwtException {
        Jws<Claims> firmado = Jwts.parser()
                .verifyWith(CLAVE)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build()
                .parseSignedClaims(token);
        if (!ALGORITMO.equals(firmado.getHeader().getAlgorithm())) {
            throw new JwtException("Algoritmo JWT no permitido");
        }
        return firmado.getPayload();
    }

    public static String rol(Claims claims) {
        return claims.get(ROL, String.class);
    }

    public record Emision(String token, Instant vence, int id, String correo, String rol) {
    }

    private static SecretKey cargarClave() {
        String secreto = configuracion("ENTREFUNCIONES_JWT_SECRET", null);
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Configure ENTREFUNCIONES_JWT_SECRET con al menos 32 bytes");
        }
        return Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    private static String configuracion(String nombre, String valorPorDefecto) {
        String valor = System.getProperty(nombre);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(nombre);
        }
        if (valor == null || valor.isBlank()) {
            // Igual que DBManager: si no hay variable de entorno, se busca en db.properties
            // (clave jwt.secret) para no obligar a configurar variables de entorno de Windows.
            try {
                java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("db");
                String clave = "jwt." + nombre.replaceFirst("^ENTREFUNCIONES_JWT_", "").toLowerCase();
                if (rb.containsKey(clave)) {
                    valor = rb.getString(clave);
                }
            } catch (java.util.MissingResourceException ignored) {
            }
        }
        return valor == null || valor.isBlank() ? valorPorDefecto : valor;
    }
}
