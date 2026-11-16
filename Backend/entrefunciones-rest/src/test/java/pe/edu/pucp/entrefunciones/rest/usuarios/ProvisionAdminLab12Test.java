package pe.edu.pucp.entrefunciones.rest.usuarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.AdministradorBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.CuentaUsuarioBLImpl;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.PasswordUtil;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EF_PROVISION_TEST_ADMIN", matches = "1")
class ProvisionAdminLab12Test {
    private static final String ESQUEMA = "entrefunciones_lab12_test";
    private static final String CORREO = "admin.katita@entrefunciones.test";

    @Test
    void creaYPruebaAdministradorSinAlterarCuentasExistentes() throws Exception {
        exigirEsquemaAislado();
        String contrasena = System.getenv("EF_TEST_ADMIN_PASSWORD");
        assertNotNull(contrasena,
                "La contrasena debe recibirse solo mediante el entorno local");
        assertTrue(contrasena.length() >= 6,
                "La contrasena debe tener al menos 6 caracteres");

        AdministradorBLImpl administradores = new AdministradorBLImpl();
        Administrador administrador = administradores.findByCorreo(CORREO);
        if (administrador == null) {
            administrador = new Administrador(0, "Katita", "Laboratorio",
                    CORREO, contrasena, "79991212", "EMP-LAB12-KATITA");
            administradores.insert(administrador);
        }

        Administrador guardado = administradores.findByCorreo(CORREO);
        assertNotNull(guardado);
        assertTrue(guardado.estaActivo());
        assertNotEquals(contrasena, guardado.getContrasena(),
                "La contrasena no debe guardarse en texto plano");
        assertTrue(PasswordUtil.verificar(contrasena, guardado.getContrasena()));
        assertInstanceOf(Administrador.class,
                new CuentaUsuarioBLImpl().autenticar(CORREO, contrasena));
        assertEquals(1, contarCuenta());
    }

    private static void exigirEsquemaAislado() throws Exception {
        if (!ESQUEMA.equals(System.getenv("DB_NAME"))) {
            throw new IllegalStateException(
                    "Se requiere exactamente entrefunciones_lab12_test");
        }
        try (Connection connection = DBManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DATABASE()");
             ResultSet result = statement.executeQuery()) {
            assertTrue(result.next());
            assertEquals(ESQUEMA, result.getString(1));
        }
    }

    private static int contarCuenta() throws Exception {
        try (Connection connection = DBManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM usuario WHERE correo=?")) {
            statement.setString(1, CORREO);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getInt(1);
            }
        }
    }
}
