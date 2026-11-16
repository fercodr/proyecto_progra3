package pe.edu.pucp.entrefunciones.rest.usuarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import pe.edu.pucp.entrefunciones.bl.impl.usuarios.CuentaUsuarioBLImpl;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.entrefunciones.rest.auth.JwtServicio;
import pe.edu.pucp.entrefunciones.rest.dto.usuarios.CredencialesDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "EF_TEST_DB", matches = "1")
class LoginRestIntegrationTest {

    @Test
    void administradorAutenticaYRecibeSesionJwtValida() throws Exception {
        exigirEsquemaAislado();
        CredencialesDTO credenciales = credencialesAdministradorDePrueba();

        Usuario usuario = new CuentaUsuarioBLImpl().autenticar(
                credenciales.getCorreo(), credenciales.getContrasena());
        JwtServicio.Emision sesion = JwtServicio.emitir(usuario);

        assertInstanceOf(Administrador.class, usuario);
        assertEquals("Administrador", sesion.rol());
        assertTrue(sesion.id() > 0);
        assertFalse(sesion.token().isBlank());
        assertEquals(Integer.toString(sesion.id()),
                JwtServicio.validar(sesion.token()).getSubject());
    }

    @Test
    void contrasenaIncorrectaDevuelve401SinSesion() throws Exception {
        exigirEsquemaAislado();
        CredencialesDTO credenciales = credencialesAdministradorDePrueba();
        credenciales.setContrasena("valor-invalido-de-prueba");

        assertNull(new CuentaUsuarioBLImpl().autenticar(
                credenciales.getCorreo(), credenciales.getContrasena()));
    }

    private static void exigirEsquemaAislado() throws Exception {
        if (!"entrefunciones_lab12_test".equals(System.getenv("DB_NAME"))) {
            throw new IllegalStateException(
                    "Se requiere exactamente entrefunciones_lab12_test");
        }
        try (Connection connection = DBManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DATABASE()");
             ResultSet result = statement.executeQuery()) {
            assertTrue(result.next());
            assertEquals("entrefunciones_lab12_test", result.getString(1),
                    "La conexion real debe usar el esquema aislado");
        }
    }

    private static CredencialesDTO credencialesAdministradorDePrueba()
            throws Exception {
        String sql = "SELECT u.correo,u.contrasena FROM usuario u "
                + "JOIN administrador a ON a.usuario_id=u.id "
                + "WHERE u.activo=TRUE ORDER BY u.id LIMIT 1";
        try (Connection connection = DBManager.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            assertTrue(result.next(),
                    "La BD de prueba debe contener un administrador activo");
            CredencialesDTO credenciales = new CredencialesDTO();
            credenciales.setCorreo(result.getString("correo"));
            credenciales.setContrasena(result.getString("contrasena"));
            return credenciales;
        }
    }
}
