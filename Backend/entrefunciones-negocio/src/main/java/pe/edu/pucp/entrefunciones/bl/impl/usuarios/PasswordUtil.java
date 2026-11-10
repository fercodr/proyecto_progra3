package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

//Cifra (hashea) contraseñas con PBKDF2, que ya viene incluido en Java: Formato guardado en la BD:  pbkdf2$iteraciones$sal$hash

public final class PasswordUtil {

    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 65536;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private PasswordUtil() { }

    /** Convierte la contraseña escrita en el texto que se guarda en la BD. */
    public static String hashear(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        new SecureRandom().nextBytes(sal);
        byte[] hash = calcular(contrasena, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /** ¿La contraseña escrita corresponde a la guardada? */
    public static boolean verificar(String contrasena, String guardada) {
        if (contrasena == null || guardada == null) {
            return false;
        }
        if (!estaHasheada(guardada)) {
            // Cuentas antiguas (datos-demo.sql) guardadas sin cifrar
            return guardada.equals(contrasena);
        }
        String[] partes = guardada.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        int iteraciones = Integer.parseInt(partes[1]);
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = calcular(contrasena, sal, iteraciones);
        return MessageDigest.isEqual(esperado, calculado);
    }

    /** Sirve para no volver a hashear algo que ya está hasheado. */
    public static boolean estaHasheada(String valor) {
        return valor != null && valor.startsWith(PREFIJO + "$");
    }

    private static byte[] calcular(String contrasena, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", e);
        }
    }
}
