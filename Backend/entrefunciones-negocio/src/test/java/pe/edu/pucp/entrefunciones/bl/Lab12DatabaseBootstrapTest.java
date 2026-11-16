package pe.edu.pucp.entrefunciones.bl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Inicializa exclusivamente la BD desechable autorizada para LAB12. */
@EnabledIfEnvironmentVariable(named = "EF_BOOTSTRAP_TEST_DB", matches = "1")
class Lab12DatabaseBootstrapTest {
    private static final String ESQUEMA_PRUEBA = "entrefunciones_lab12_test";
    private static final Pattern USE = Pattern.compile(
            "(?im)^\\s*USE\\s+`?([A-Za-z0-9_]+)`?\\s*;");
    private static final Pattern DATABASE = Pattern.compile(
            "(?im)^\\s*(?:CREATE|DROP)\\s+DATABASE(?:\\s+IF\\s+(?:NOT\\s+)?EXISTS)?\\s+`?([A-Za-z0-9_]+)`?");

    @Test
    void crearEInicializarEsquemaAislado() throws Exception {
        String solicitado = System.getenv("DB_NAME");
        assertEquals(ESQUEMA_PRUEBA, solicitado,
                "La inicialización solo admite el esquema aislado de LAB12");

        ResourceBundle configuracion = ResourceBundle.getBundle("db");
        String host = valor("DB_HOST", configuracion, "servidor");
        String puerto = valor("DB_PORT", configuracion, "puerto");
        String usuario = valor("DB_USER", configuracion, "usuario");
        String password = valor("DB_PASSWORD", configuracion, "password");
        Class.forName("com.mysql.cj.jdbc.Driver");

        String opciones = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima";
        try (Connection servidor = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + puerto + "/" + opciones,
                usuario, password);
             Statement sql = servidor.createStatement()) {
            // El nombre es una constante validada; jamás se toma libremente del entorno.
            sql.executeUpdate("DROP DATABASE IF EXISTS `" + ESQUEMA_PRUEBA + "`");
            sql.executeUpdate("CREATE DATABASE `" + ESQUEMA_PRUEBA
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        try (Connection prueba = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + puerto + "/" + ESQUEMA_PRUEBA + opciones,
                usuario, password)) {
            Path directorioSql = Path.of(System.getProperty("user.dir"), "..", "sql")
                    .normalize().toAbsolutePath();
            for (String archivo : List.of(
                    "entrefunciones-mysql.sql", "procedimientos.sql", "datos-demo.sql")) {
                String contenido = Files.readString(
                        directorioSql.resolve(archivo), StandardCharsets.UTF_8);
                ejecutarScript(prueba, aislar(contenido, archivo));
            }

            try (Statement sql = prueba.createStatement();
                 ResultSet rs = sql.executeQuery(
                         "SELECT DATABASE(), "
                                 + "(SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()), "
                                 + "(SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema=DATABASE())")) {
                assertTrue(rs.next());
                assertEquals(ESQUEMA_PRUEBA, rs.getString(1));
                assertTrue(rs.getInt(2) >= 15, "El esquema de prueba no contiene todas las tablas");
                assertTrue(rs.getInt(3) >= 20, "No se instalaron los procedimientos de prueba");
            }
        }
    }

    private static String valor(String variable, ResourceBundle rb, String clave) {
        String valor = System.getProperty(variable, System.getenv(variable));
        return valor == null || valor.isBlank() ? rb.getString(clave) : valor;
    }

    private static String aislar(String original, String archivo) {
        if (original.matches("(?is).*\\bDB2\\b.*")) {
            throw new IllegalStateException(archivo + " contiene una referencia a DB2");
        }
        String aislado = original
                .replace("CREATE DATABASE IF NOT EXISTS entrefunciones",
                        "CREATE DATABASE IF NOT EXISTS `" + ESQUEMA_PRUEBA + "`")
                .replace("USE entrefunciones;", "USE `" + ESQUEMA_PRUEBA + "`;");
        validarEsquemas(aislado, archivo, USE);
        validarEsquemas(aislado, archivo, DATABASE);
        return aislado;
    }

    private static void validarEsquemas(
            String script, String archivo, Pattern patron) {
        Matcher matcher = patron.matcher(script);
        while (matcher.find()) {
            if (!ESQUEMA_PRUEBA.equals(matcher.group(1))) {
                throw new IllegalStateException(
                        archivo + " intenta operar sobre el esquema " + matcher.group(1));
            }
        }
    }

    private static void ejecutarScript(Connection conexion, String script) throws Exception {
        String delimitador = ";";
        StringBuilder sentencia = new StringBuilder();
        try (Statement sql = conexion.createStatement()) {
            for (String linea : script.replace("\r", "").split("\n", -1)) {
                String limpia = linea.trim();
                if (limpia.toUpperCase().startsWith("DELIMITER ")) {
                    if (!sentencia.toString().isBlank()) {
                        throw new IllegalStateException("Cambio de delimitador dentro de una sentencia");
                    }
                    delimitador = limpia.substring("DELIMITER ".length()).trim();
                    continue;
                }

                sentencia.append(linea).append('\n');
                String paraDetectar = linea.replaceFirst("--.*$", "").trim();
                if (!paraDetectar.endsWith(delimitador)) {
                    continue;
                }

                String completa = sentencia.toString();
                int fin = completa.lastIndexOf(delimitador);
                completa = completa.substring(0, fin) + completa.substring(fin + delimitador.length());
                if (!completa.isBlank() && !completa.strip().startsWith("--") || completa.contains("\n")) {
                    sql.execute(completa);
                }
                sentencia.setLength(0);
            }
        }
        if (!sentencia.toString().isBlank()) {
            throw new IllegalStateException("El script terminó con una sentencia incompleta");
        }
    }
}
