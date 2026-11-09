package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.db.DBManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SedeTransaccionIntegracionTest {
    private Field campoInstancia;
    private DBManager instanciaOriginal;
    private String url;

    @BeforeEach
    void prepararBaseEnMemoria() throws Exception {
        url = "jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        crearEsquema();

        campoInstancia = DBManager.class.getDeclaredField("instance");
        campoInstancia.setAccessible(true);
        instanciaOriginal = (DBManager) campoInstancia.get(null);
        campoInstancia.set(null, new DBManagerH2(url));
    }

    @AfterEach
    void restaurarDBManager() throws Exception {
        if (TransactionsManager.activa()) {
            TransactionsManager.rollback();
        }
        campoInstancia.set(null, instanciaOriginal);
    }

    @Test
    void reviertePadreYPrimerHijoSiFallaElSegundoHijo() throws SQLException {
        Sede sede = sedeConDosSalas();
        SedeBLImpl bl = new SedeBLImpl(
                new SedeDAOH2(), new SalaDAOConFallaEnSegundoInsert(), new ButacaDAOImpl());

        assertThrows(BLException.class, () -> bl.insert(sede));

        assertEquals(0, contar("sede"));
        assertEquals(0, contar("sala"));
        assertEquals(0, sede.getId());
        assertEquals(0, sede.getSalas().get(0).getId());
        assertFalse(TransactionsManager.activa());
    }

    @Test
    void persisteYCargaLaColeccionCompletaConLasMismasReferencias() throws Exception {
        Sede sede = nuevaSede();
        Sala sala = new Sala(0, sede, "Principal", 2, "Numerada");
        Butaca butaca = new Butaca(0, sala, "A", 1, "General");
        sala.agregarButaca(butaca);
        sede.agregarSala(sala);
        SedeBLImpl bl = new SedeBLImpl(
                new SedeDAOH2(), new SalaDAOH2(), new ButacaDAOImpl());

        bl.insert(sede);
        Sede cargada = bl.findByIdCompleto(sede.getId());

        assertNotNull(cargada);
        assertEquals(1, cargada.getSalas().size());
        Sala salaCargada = cargada.getSalas().get(0);
        assertSame(cargada, salaCargada.getSede());
        assertEquals(1, salaCargada.getButacas().size());
        assertSame(salaCargada, salaCargada.getButacas().get(0).getSala());
    }

    @Test
    void updateConListaVaciaConservaHijosYSincronizacionDesactivaOmitidos()
            throws Exception {
        SedeBLImpl bl = new SedeBLImpl(
                new SedeDAOH2(), new SalaDAOH2(), new ButacaDAOImpl());
        Sede sede = sedeConDosSalas();
        bl.insert(sede);

        Sede soloCampos = new Sede(
                sede.getId(), "EntreFunciones Actualizada",
                sede.getDireccion(), sede.getCiudad(), sede.getTelefonoContacto());
        bl.update(soloCampos);
        Sede despuesDeUpdate = bl.findByIdCompleto(sede.getId());
        assertEquals(2, despuesDeUpdate.getSalas().stream().filter(Sala::estaActivo).count());

        Sala primera = despuesDeUpdate.getSalas().get(0);
        Sede coleccionFinal = new Sede(
                sede.getId(), soloCampos.getRazonSocial(),
                sede.getDireccion(), sede.getCiudad(), sede.getTelefonoContacto());
        coleccionFinal.agregarSala(new Sala(
                primera.getId(), coleccionFinal, primera.getNombre(),
                primera.getAforo(), primera.getTipoSala()));
        bl.sincronizarSalas(coleccionFinal);

        Sede despuesDeSincronizar = bl.findByIdCompleto(sede.getId());
        assertEquals(2, despuesDeSincronizar.getSalas().size());
        assertEquals(1, despuesDeSincronizar.getSalas().stream().filter(Sala::estaActivo).count());
    }

    private Sede sedeConDosSalas() {
        Sede sede = nuevaSede();
        sede.agregarSala(new Sala(0, sede, "Uno", 10, "Numerada"));
        sede.agregarSala(new Sala(0, sede, "Dos", 10, "Numerada"));
        return sede;
    }

    private Sede nuevaSede() {
        return new Sede(0, "EntreFunciones", "Av. Principal 123", "Lima", "555-0101");
    }

    private void crearEsquema() throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE sede (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        razon_social VARCHAR(150) NOT NULL,
                        direccion VARCHAR(200) NOT NULL,
                        ciudad VARCHAR(100) NOT NULL,
                        telefono_contacto VARCHAR(20),
                        activo BOOLEAN NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE sala (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        sede_id INT NOT NULL,
                        nombre VARCHAR(100) NOT NULL,
                        aforo INT NOT NULL,
                        tipo_sala VARCHAR(50) NOT NULL,
                        activo BOOLEAN NOT NULL,
                        CONSTRAINT fk_sala_sede FOREIGN KEY (sede_id) REFERENCES sede(id),
                        CONSTRAINT uq_sala_nombre UNIQUE (sede_id, nombre)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE butaca (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        sala_id INT NOT NULL,
                        fila VARCHAR(5) NOT NULL,
                        columna INT NOT NULL,
                        tipo_butaca VARCHAR(30) NOT NULL,
                        activo BOOLEAN NOT NULL,
                        CONSTRAINT fk_butaca_sala FOREIGN KEY (sala_id) REFERENCES sala(id),
                        CONSTRAINT uq_butaca_posicion UNIQUE (sala_id, fila, columna)
                    )
                    """);
        }
    }

    private int contar(String tabla) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             var rs = statement.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static final class SalaDAOConFallaEnSegundoInsert extends SalaDAOH2 {
        private int inserciones;

        @Override
        public void insert(Sala sala) throws SQLException {
            inserciones++;
            if (inserciones == 2) {
                throw new SQLException("Fallo deliberado al escribir el segundo hijo");
            }
            super.insert(sala);
        }
    }

    private static class SalaDAOH2 extends SalaDAOImpl {
        @Override
        public List<Sala> findBySedeId(int sedeId) throws SQLException {
            String sql = """
                    SELECT id, nombre, aforo, tipo_sala, activo
                    FROM sala
                    WHERE sede_id = ?
                    ORDER BY id
                    """;
            try (ConnectionScope scope = ConnectionScope.openForRead();
                 var cmd = scope.connection().prepareStatement(sql)) {
                cmd.setInt(1, sedeId);
                try (var rs = cmd.executeQuery()) {
                    var salas = new java.util.ArrayList<Sala>();
                    while (rs.next()) {
                        Sede sede = new Sede();
                        sede.setId(sedeId);
                        Sala sala = new Sala(
                                rs.getInt("id"), sede, rs.getString("nombre"),
                                rs.getInt("aforo"), rs.getString("tipo_sala"));
                        if (rs.getBoolean("activo")) {
                            sala.activar();
                        } else {
                            sala.desactivar();
                        }
                        salas.add(sala);
                    }
                    return salas;
                }
            }
        }

        @Override
        public void insert(Sala sala) throws SQLException {
            String sql = """
                    INSERT INTO sala (sede_id, nombre, aforo, tipo_sala, activo)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            Connection connection = TransactionsManager.getConnection();
            try (var cmd = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                asignarParametros(cmd, sala);
                cmd.executeUpdate();
                try (var keys = cmd.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se obtuvo el id de la sala");
                    }
                    sala.setId(keys.getInt(1));
                }
            }
        }

        @Override
        public void update(Sala sala) throws SQLException {
            String sql = """
                    UPDATE sala
                    SET sede_id = ?, nombre = ?, aforo = ?, tipo_sala = ?, activo = ?
                    WHERE id = ?
                    """;
            Connection connection = TransactionsManager.getConnection();
            try (var cmd = connection.prepareStatement(sql)) {
                asignarParametros(cmd, sala);
                cmd.setInt(6, sala.getId());
                cmd.executeUpdate();
            }
        }

        @Override
        public void delete(Integer id) throws SQLException {
            Connection connection = TransactionsManager.getConnection();
            try (var cmd = connection.prepareStatement(
                    "UPDATE sala SET activo = FALSE WHERE id = ?")) {
                cmd.setInt(1, id);
                cmd.executeUpdate();
            }
        }

        private void asignarParametros(java.sql.PreparedStatement cmd, Sala sala)
                throws SQLException {
            cmd.setInt(1, sala.getSede().getId());
            cmd.setString(2, sala.getNombre());
            cmd.setInt(3, sala.getAforo());
            cmd.setString(4, sala.getTipoSala());
            cmd.setBoolean(5, sala.estaActivo());
        }
    }

    private static final class SedeDAOH2 extends SedeDAOImpl {
        @Override
        public Sede findById(Integer id) throws SQLException {
            String sql = """
                    SELECT id, razon_social, direccion, ciudad, telefono_contacto, activo
                    FROM sede
                    WHERE id = ?
                    """;
            try (ConnectionScope scope = ConnectionScope.openForRead();
                 var cmd = scope.connection().prepareStatement(sql)) {
                cmd.setInt(1, id);
                try (var rs = cmd.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Sede sede = new Sede(
                            rs.getInt("id"), rs.getString("razon_social"),
                            rs.getString("direccion"), rs.getString("ciudad"),
                            rs.getString("telefono_contacto"));
                    if (rs.getBoolean("activo")) {
                        sede.activar();
                    } else {
                        sede.desactivar();
                    }
                    return sede;
                }
            }
        }

        @Override
        public Sede findByIdCompleto(Integer id) throws SQLException {
            Sede sede = findById(id);
            if (sede == null) {
                return null;
            }
            SalaDAOH2 salaDAO = new SalaDAOH2();
            ButacaDAOImpl butacaDAO = new ButacaDAOImpl();
            for (Sala sala : salaDAO.findBySedeId(id)) {
                sala.setSede(sede);
                for (Butaca butaca : butacaDAO.findBySalaId(sala.getId())) {
                    butaca.setSala(sala);
                    sala.agregarButaca(butaca);
                }
                sede.agregarSala(sala);
            }
            return sede;
        }

        @Override
        public void insert(Sede sede) throws SQLException {
            String sql = """
                    INSERT INTO sede
                        (razon_social, direccion, ciudad, telefono_contacto, activo)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            Connection connection = TransactionsManager.getConnection();
            try (var cmd = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                asignarParametros(cmd, sede);
                cmd.executeUpdate();
                try (var keys = cmd.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se obtuvo el id de la sede");
                    }
                    sede.setId(keys.getInt(1));
                }
            }
        }

        @Override
        public void update(Sede sede) throws SQLException {
            String sql = """
                    UPDATE sede
                    SET razon_social = ?, direccion = ?, ciudad = ?,
                        telefono_contacto = ?, activo = ?
                    WHERE id = ?
                    """;
            Connection connection = TransactionsManager.getConnection();
            try (var cmd = connection.prepareStatement(sql)) {
                asignarParametros(cmd, sede);
                cmd.setInt(6, sede.getId());
                cmd.executeUpdate();
            }
        }

        private void asignarParametros(java.sql.PreparedStatement cmd, Sede sede)
                throws SQLException {
            cmd.setString(1, sede.getRazonSocial());
            cmd.setString(2, sede.getDireccion());
            cmd.setString(3, sede.getCiudad());
            cmd.setString(4, sede.getTelefonoContacto());
            cmd.setBoolean(5, sede.estaActivo());
        }
    }

    private static final class DBManagerH2 extends DBManager {
        private final String url;

        private DBManagerH2(String url) {
            this.url = url;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url);
        }
    }
}
