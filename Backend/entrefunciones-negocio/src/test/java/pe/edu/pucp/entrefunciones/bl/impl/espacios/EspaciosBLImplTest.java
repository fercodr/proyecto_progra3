package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EspaciosBLImplTest {

    @BeforeEach
    void iniciarTransaccionDePrueba() {
        TransactionsManager.iniciar(conexionFalsa());
    }

    @AfterEach
    void cerrarTransaccionDePrueba() {
        if (TransactionsManager.activa()) {
            TransactionsManager.rollback();
        }
    }

    @Test
    void salaRechazaNombreDuplicadoEnLaMismaSede() {
        Contexto contexto = new Contexto();
        contexto.salaDAO.guardar(sala(1, contexto.sede, "Principal", 20));
        SalaBLImpl bl = contexto.salaBL();

        BLException error = assertThrows(BLException.class,
                () -> bl.insert(sala(0, contexto.sede, " principal ", 30)));

        assertTrue(error.getMessage().contains("Ya existe una sala"));
    }

    @Test
    void salaPuedeActualizarseConservandoSuPropioNombre() {
        Contexto contexto = new Contexto();
        Sala registrada = sala(1, contexto.sede, "Principal", 20);
        contexto.salaDAO.guardar(registrada);
        Sala actualizada = sala(1, contexto.sede, "Principal", 25);

        assertDoesNotThrow(() -> contexto.salaBL().update(actualizada));
        assertEquals(25, contexto.salaDAO.salas.get(1).getAforo());
    }

    @Test
    void salaNoReduceAforoDebajoDeButacasActivas() {
        Contexto contexto = new Contexto();
        Sala registrada = sala(1, contexto.sede, "Principal", 3);
        contexto.salaDAO.guardar(registrada);
        contexto.butacaDAO.guardar(butaca(1, registrada, "A", 1));
        contexto.butacaDAO.guardar(butaca(2, registrada, "A", 2));

        BLException error = assertThrows(BLException.class,
                () -> contexto.salaBL().update(sala(1, contexto.sede, "Principal", 1)));

        assertTrue(error.getMessage().contains("aforo"));
    }

    @Test
    void butacaNoSeConfundeConsigoMismaAlActualizar() {
        Contexto contexto = new Contexto();
        Sala sala = sala(1, contexto.sede, "Principal", 3);
        contexto.salaDAO.guardar(sala);
        Butaca registrada = butaca(1, sala, "A", 1);
        contexto.butacaDAO.guardar(registrada);

        assertDoesNotThrow(() -> contexto.butacaBL().update(butaca(1, sala, "A", 1)));
        assertEquals(1, contexto.butacaDAO.actualizaciones);
    }

    @Test
    void butacaRechazaPosicionOcupadaPorOtroRegistro() {
        Contexto contexto = new Contexto();
        Sala sala = sala(1, contexto.sede, "Principal", 3);
        contexto.salaDAO.guardar(sala);
        contexto.butacaDAO.guardar(butaca(1, sala, "A", 1));
        contexto.butacaDAO.guardar(butaca(2, sala, "A", 2));

        BLException error = assertThrows(BLException.class,
                () -> contexto.butacaBL().update(butaca(2, sala, "A", 1)));

        assertTrue(error.getMessage().contains("otra butaca"));
    }

    @Test
    void insertarEnPosicionInactivaReutilizaElRegistro() {
        Contexto contexto = new Contexto();
        Sala sala = sala(1, contexto.sede, "Principal", 3);
        contexto.salaDAO.guardar(sala);
        Butaca inactiva = butaca(7, sala, "A", 1);
        inactiva.desactivar();
        contexto.butacaDAO.guardar(inactiva);
        Butaca nueva = butaca(0, sala, "A", 1);

        assertDoesNotThrow(() -> contexto.butacaBL().insert(nueva));

        assertEquals(7, nueva.getId());
        assertEquals(1, contexto.butacaDAO.actualizaciones);
        assertEquals(0, contexto.butacaDAO.inserciones);
    }

    @Test
    void butacaActivaNoPuedeSuperarElAforo() {
        Contexto contexto = new Contexto();
        Sala sala = sala(1, contexto.sede, "Principal", 1);
        contexto.salaDAO.guardar(sala);
        contexto.butacaDAO.guardar(butaca(1, sala, "A", 1));

        BLException error = assertThrows(BLException.class,
                () -> contexto.butacaBL().insert(butaca(0, sala, "A", 2)));

        assertTrue(error.getMessage().contains("aforo"));
    }

    @Test
    void fallaSqlConservaLaCausa() {
        SedeDAOConFalla dao = new SedeDAOConFalla();
        SedeBLImpl bl = new SedeBLImpl(dao);

        BLException error = assertThrows(BLException.class, bl::findAll);

        assertNotNull(error.getCause());
        assertTrue(error.getCause() instanceof SQLException);
    }

    private static Sede sede(int id) {
        return new Sede(id, "EntreFunciones", "Av. Principal 123", "Lima", "555-0101");
    }

    private static Sala sala(int id, Sede sede, String nombre, int aforo) {
        return new Sala(id, sede, nombre, aforo, "Numerada");
    }

    private static Butaca butaca(int id, Sala sala, String fila, int columna) {
        return new Butaca(id, sala, fila, columna, "General");
    }

    private static Connection conexionFalsa() {
        return (Connection) Proxy.newProxyInstance(
                EspaciosBLImplTest.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, metodo, argumentos) -> switch (metodo.getName()) {
                    case "getAutoCommit" -> false;
                    case "isClosed" -> false;
                    case "isWrapperFor" -> false;
                    case "unwrap" -> null;
                    case "toString" -> "ConexionTransaccionalFalsa";
                    default -> valorPredeterminado(metodo.getReturnType());
                });
    }

    private static Object valorPredeterminado(Class<?> tipo) {
        if (!tipo.isPrimitive() || tipo == void.class) {
            return null;
        }
        if (tipo == boolean.class) {
            return false;
        }
        if (tipo == char.class) {
            return '\0';
        }
        return 0;
    }

    private static final class Contexto {
        private final Sede sede = sede(1);
        private final SedeDAOFalso sedeDAO = new SedeDAOFalso();
        private final SalaDAOFalso salaDAO = new SalaDAOFalso();
        private final ButacaDAOFalso butacaDAO = new ButacaDAOFalso();

        private Contexto() {
            sedeDAO.guardar(sede);
        }

        private SalaBLImpl salaBL() {
            return new SalaBLImpl(salaDAO, sedeDAO, butacaDAO);
        }

        private ButacaBLImpl butacaBL() {
            return new ButacaBLImpl(butacaDAO, salaDAO);
        }


    }

    private static class SedeDAOFalso implements SedeDAO {
        protected final Map<Integer, Sede> sedes = new LinkedHashMap<>();
        protected int secuencia = 1;
        protected int eliminaciones;

        void guardar(Sede sede) {
            sedes.put(sede.getId(), sede);
            secuencia = Math.max(secuencia, sede.getId() + 1);
        }

        @Override public List<Sede> findAll() throws SQLException { return new ArrayList<>(sedes.values()); }
        @Override public Sede findById(Integer id) throws SQLException { return sedes.get(id); }
        @Override public Sede findByIdCompleto(Integer id) throws SQLException { return sedes.get(id); }
        @Override public List<Sede> findByIds(Collection<Integer> ids) throws SQLException {
            return ids.stream().map(sedes::get).filter(Objects::nonNull).toList();
        }
        @Override public void insert(Sede sede) throws SQLException { sede.setId(secuencia++); guardar(sede); }
        @Override public void update(Sede sede) throws SQLException { guardar(sede); }
        @Override public void delete(Integer id) throws SQLException { sedes.get(id).desactivar(); eliminaciones++; }
    }

    private static final class SedeDAOConFalla extends SedeDAOFalso {
        @Override public List<Sede> findAll() throws SQLException { throw new SQLException("sin conexión"); }
    }

    private static final class SalaDAOFalso implements SalaDAO {
        private final Map<Integer, Sala> salas = new LinkedHashMap<>();
        private int secuencia = 1;

        void guardar(Sala sala) {
            salas.put(sala.getId(), sala);
            secuencia = Math.max(secuencia, sala.getId() + 1);
        }

        @Override public List<Sala> findAll() { return new ArrayList<>(salas.values()); }
        @Override public List<Sala> findBySedeId(int sedeId) {
            return salas.values().stream().filter(s -> s.getSede().getId() == sedeId).toList();
        }
        @Override public List<Sala> findByIds(Collection<Integer> ids) {
            return ids.stream().map(salas::get).filter(Objects::nonNull).toList();
        }
        @Override public Sala findById(Integer id) { return salas.get(id); }
        @Override public void insert(Sala sala) { sala.setId(secuencia++); guardar(sala); }
        @Override public void update(Sala sala) { guardar(sala); }
        @Override public void delete(Integer id) { salas.get(id).desactivar(); }
    }

    private static final class ButacaDAOFalso implements ButacaDAO {
        private final Map<Integer, Butaca> butacas = new LinkedHashMap<>();
        private int secuencia = 1;
        private int inserciones;
        private int actualizaciones;

        void guardar(Butaca butaca) {
            butacas.put(butaca.getId(), butaca);
            secuencia = Math.max(secuencia, butaca.getId() + 1);
        }

        @Override public List<Butaca> findAll() { return new ArrayList<>(butacas.values()); }
        @Override public List<Butaca> findBySalaId(int salaId) {
            return butacas.values().stream().filter(b -> b.getSala().getId() == salaId).toList();
        }
        @Override public List<Butaca> findByReservaId(int reservaId) { return List.of(); }
        @Override public Butaca findById(Integer id) { return butacas.get(id); }
        @Override public void insert(Butaca butaca) { inserciones++; butaca.setId(secuencia++); guardar(butaca); }
        @Override public void update(Butaca butaca) { actualizaciones++; guardar(butaca); }
        @Override public void delete(Integer id) { butacas.get(id).desactivar(); }
    }

}
