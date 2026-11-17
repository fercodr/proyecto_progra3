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
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Instala solo el procedimiento focal en el esquema aislado autorizado. */
@EnabledIfEnvironmentVariable(named = "EF_TEST_DB", matches = "1")
class ReporteFechasOpcionalesMigrationTest {
    private static final String ESQUEMA_PRUEBA = "entrefunciones_lab12_test";

    @Test
    void instalaProcedimientoEnEsquemaAislado() throws Exception {
        assertEquals(ESQUEMA_PRUEBA, System.getenv("DB_NAME"),
                "La migracion focal solo admite entrefunciones_lab12_test");

        ResourceBundle configuracion = ResourceBundle.getBundle("db");
        String host = valor("DB_HOST", configuracion, "servidor");
        String puerto = valor("DB_PORT", configuracion, "puerto");
        String usuario = valor("DB_USER", configuracion, "usuario");
        String password = valor("DB_PASSWORD", configuracion, "password");
        Class.forName("com.mysql.cj.jdbc.Driver");

        String opciones = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima";
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + puerto + "/" + ESQUEMA_PRUEBA + opciones,
                usuario, password)) {
            try (Statement sql = conexion.createStatement();
                 ResultSet rs = sql.executeQuery("SELECT DATABASE()")) {
                assertTrue(rs.next());
                assertEquals(ESQUEMA_PRUEBA, rs.getString(1));
            }

            Path migracion = Path.of(System.getProperty("user.dir"), "..", "sql", "migraciones",
                    "20261116_reportes_fechas_opcionales.sql").normalize().toAbsolutePath();
            ejecutarScript(conexion, Files.readString(migracion, StandardCharsets.UTF_8));

            try (Statement sql = conexion.createStatement();
                 ResultSet rs = sql.executeQuery("SHOW CREATE PROCEDURE listar_reportes_funciones")) {
                assertTrue(rs.next());
                String definicion = rs.getString("Create Procedure");
                assertTrue(definicion.contains("p_desde IS NULL OR f.fecha >= p_desde"));
                assertTrue(definicion.contains("p_hasta IS NULL OR f.fecha <= p_hasta"));
            }
        }
    }

    private static String valor(String variable, ResourceBundle rb, String clave) {
        String valor = System.getProperty(variable, System.getenv(variable));
        return valor == null || valor.isBlank() ? rb.getString(clave) : valor;
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
                completa = completa.substring(0, fin)
                        + completa.substring(fin + delimitador.length());
                if (!completa.isBlank()) {
                    sql.execute(completa);
                }
                sentencia.setLength(0);
            }
        }
        if (!sentencia.toString().isBlank()) {
            throw new IllegalStateException("El script termino con una sentencia incompleta");
        }
    }
}
