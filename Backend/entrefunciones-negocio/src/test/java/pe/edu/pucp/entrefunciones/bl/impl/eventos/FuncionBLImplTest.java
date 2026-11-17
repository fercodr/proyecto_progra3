package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ActividadCultural;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ObraTeatro;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.catalogo.TipoActividad;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FuncionBLImplTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 17);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 19);

    @Test
    void combinaSedePeriodoYTipoSobreElMismoConjunto() throws Exception {
        Contexto contexto = new Contexto();

        List<Funcion> resultado = contexto.bl.findReportes(
                DESDE, HASTA, 1, null, "PELICULA");

        assertEquals(List.of(1), resultado.stream().map(Funcion::getId).toList());
        assertEquals(new BigDecimal("120.00"), resultado.stream()
                .map(Funcion::getIngresosConfirmados)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(3, resultado.stream()
                .mapToInt(Funcion::getEntradasVigentes).sum());
        assertEquals(5, resultado.stream()
                .mapToInt(Funcion::getEntradasEmitidas).sum());
        assertEquals(1, resultado.stream()
                .mapToInt(Funcion::getEntradasCanceladas).sum());
        assertEquals(1, contexto.funcionDAO.ultimaSede);
        assertEquals("PELICULA", contexto.funcionDAO.ultimoTipoEvento);
        assertEquals(DESDE, contexto.funcionDAO.ultimoDesde);
        assertEquals(HASTA, contexto.funcionDAO.ultimoHasta);
    }

    @Test
    void todosOmiteElTipoYConservaLosDemasFiltros() throws Exception {
        Contexto contexto = new Contexto();

        List<Funcion> resultado = contexto.bl.findReportes(
                DESDE, HASTA, 1, null, null);

        assertEquals(List.of(1, 2), resultado.stream().map(Funcion::getId).toList());
        assertNull(contexto.funcionDAO.ultimoTipoEvento);
    }

    @Test
    void combinacionSinCoincidenciasNoRecuperaTodosLosDatos() throws Exception {
        Contexto contexto = new Contexto();

        List<Funcion> resultado = contexto.bl.findReportes(
                DESDE, HASTA, 1, null, "ACTIVIDAD_CULTURAL");

        assertEquals(List.of(), resultado);
        assertEquals(BigDecimal.ZERO, resultado.stream()
                .map(Funcion::getIngresosConfirmados)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(0, resultado.stream()
                .mapToInt(Funcion::getEntradasVigentes).sum());
        assertEquals("ACTIVIDAD_CULTURAL", contexto.funcionDAO.ultimoTipoEvento);
    }

    @Test
    void rechazaUnTipoFueraDeLosValoresDelCatalogo() {
        Contexto contexto = new Contexto();

        assertThrows(ValidacionException.class, () -> contexto.bl.findReportes(
                DESDE, HASTA, 1, null, "CONCIERTO"));
    }

    @Test
    void aceptaNingunoOUnoDeLosLimitesDeFecha() throws Exception {
        Contexto contexto = new Contexto();

        assertEquals(List.of(1, 2, 4), contexto.bl.findReportes(
                null, null, 1, null, null).stream().map(Funcion::getId).toList());
        assertEquals(List.of(4), contexto.bl.findReportes(
                LocalDate.of(2026, 11, 25), null, 1, null, null)
                .stream().map(Funcion::getId).toList());
        assertEquals(List.of(1, 2), contexto.bl.findReportes(
                null, LocalDate.of(2026, 11, 18), 1, null, null)
                .stream().map(Funcion::getId).toList());
    }

    @Test
    void rechazaRangoDeFechaInvertido() {
        Contexto contexto = new Contexto();

        assertThrows(ValidacionException.class, () -> contexto.bl.findReportes(
                HASTA, DESDE, 1, null, null));
    }

    @Test
    void ocupacionUsaEntradasVigentesYConservaElTotalEmitido() throws Exception {
        Contexto contexto = new Contexto();

        Funcion funcion = contexto.bl.findReportes(
                DESDE, HASTA, 1, null, "PELICULA").get(0);

        assertEquals(3, funcion.getEntradasVigentes());
        assertEquals(5, funcion.getEntradasEmitidas());
        assertEquals(1, funcion.getEntradasCanceladas());
        assertEquals(new BigDecimal("3.00"), funcion.getPorcentajeOcupacion());
    }

    private static final class Contexto {
        private final Map<Integer, Sede> sedes = new LinkedHashMap<>();
        private final Map<Integer, Sala> salas = new LinkedHashMap<>();
        private final Map<Integer, Evento> eventos = new LinkedHashMap<>();
        private final FuncionDAOFalso funcionDAO = new FuncionDAOFalso();
        private final FuncionBLImpl bl;

        private Contexto() {
            Sede sedeUno = sede(1, "Centro");
            Sede sedeDos = sede(2, "Norte");
            Sala salaUno = sala(1, sedeUno, "Principal");
            Sala salaDos = sala(2, sedeDos, "Cultural");
            sedes.put(sedeUno.getId(), sedeUno);
            sedes.put(sedeDos.getId(), sedeDos);
            salas.put(salaUno.getId(), salaUno);
            salas.put(salaDos.getId(), salaDos);

            Evento pelicula = new Pelicula(1, 40, "Película", 100,
                    CategoriaGenero.values()[0], ClasificacionEdad.values()[0], "Dirección");
            Evento teatro = new ObraTeatro(2, 35, "Obra", "Dirección", "Elenco");
            Evento actividad = new ActividadCultural(3, 25, "Actividad", "Expositor",
                    TipoActividad.values()[0]);
            Evento peliculaFueraDelPeriodo = new Pelicula(4, 30, "Otra película", 90,
                    CategoriaGenero.values()[0], ClasificacionEdad.values()[0], "Dirección");
            eventos.put(pelicula.getId(), pelicula);
            eventos.put(teatro.getId(), teatro);
            eventos.put(actividad.getId(), actividad);
            eventos.put(peliculaFueraDelPeriodo.getId(), peliculaFueraDelPeriodo);

            funcionDAO.funciones.add(funcion(
                    1, pelicula, salaUno, LocalDate.of(2026, 11, 18),
                    "120.00", 3, 5, 1));
            funcionDAO.funciones.add(funcion(
                    2, teatro, salaUno, LocalDate.of(2026, 11, 18),
                    "70.00", 2, 2, 0));
            funcionDAO.funciones.add(funcion(
                    3, actividad, salaDos, LocalDate.of(2026, 11, 18),
                    "50.00", 2, 2, 0));
            funcionDAO.funciones.add(funcion(
                    4, peliculaFueraDelPeriodo, salaUno, LocalDate.of(2026, 11, 25),
                    "30.00", 1, 1, 0));

            bl = new FuncionBLImpl(
                    funcionDAO,
                    daoLectura(EventoDAO.class, eventos),
                    daoLectura(SalaDAO.class, salas),
                    daoLectura(SedeDAO.class, sedes));
        }
    }

    private static Funcion funcion(int id, Evento evento, Sala sala,
                                   LocalDate fecha, String ingresos,
                                   int vigentes, int emitidas, int canceladas) {
        Funcion funcion = new Funcion(id, evento, sala, fecha,
                LocalTime.of(18, 0), LocalTime.of(20, 0), "Español");
        funcion.setIngresosConfirmados(new BigDecimal(ingresos));
        funcion.setEntradasVigentes(vigentes);
        funcion.setEntradasEmitidas(emitidas);
        funcion.setEntradasCanceladas(canceladas);
        return funcion;
    }

    private static Sede sede(int id, String nombre) {
        return new Sede(id, nombre, "Av. Principal 123", "Lima", "555-0101");
    }

    private static Sala sala(int id, Sede sede, String nombre) {
        return new Sala(id, sede, nombre, 100, "Numerada");
    }

    private static String tipoEvento(Evento evento) {
        if (evento instanceof Pelicula) {
            return "PELICULA";
        }
        if (evento instanceof ObraTeatro) {
            return "OBRA_TEATRO";
        }
        return "ACTIVIDAD_CULTURAL";
    }

    private static final class FuncionDAOFalso implements FuncionDAO {
        private final List<Funcion> funciones = new ArrayList<>();
        private LocalDate ultimoDesde;
        private LocalDate ultimoHasta;
        private Integer ultimaSede;
        private String ultimoTipoEvento;

        @Override
        public List<Funcion> findReportes(LocalDate desde, LocalDate hasta,
                                          Integer idSede, Integer idSala,
                                          String tipoEvento) {
            ultimoDesde = desde;
            ultimoHasta = hasta;
            ultimaSede = idSede;
            ultimoTipoEvento = tipoEvento;
            return funciones.stream()
                    .filter(f -> desde == null || !f.getFecha().isBefore(desde))
                    .filter(f -> hasta == null || !f.getFecha().isAfter(hasta))
                    .filter(f -> idSede == null || f.getSala().getSede().getId() == idSede)
                    .filter(f -> idSala == null || f.getSala().getId() == idSala)
                    .filter(f -> tipoEvento == null || tipoEvento(f.getEvento()).equals(tipoEvento))
                    .toList();
        }

        @Override public List<Funcion> findAll() { return List.copyOf(funciones); }
        @Override public Funcion findById(Integer id) { return funciones.stream()
                .filter(f -> f.getId() == id).findFirst().orElse(null); }
        @Override public void insert(Funcion modelo) { funciones.add(modelo); }
        @Override public void update(Funcion modelo) { }
        @Override public void delete(Integer id) { }
        @Override public List<Funcion> findAllActiveFunctions() { return List.copyOf(funciones); }
        @Override public void insertFunciones(int idEvento, List<Funcion> funciones) { }
        @Override public void updateFunciones(int idEvento, List<Funcion> funciones) { }
        @Override public void deleteFunciones(int idEvento) { }
        @Override public List<Funcion> findByEvento(Evento evento) { return funciones.stream()
                .filter(f -> f.getEvento().getId() == evento.getId()).toList(); }
        @Override public List<Funcion> findBySalaId(int idSala) { return funciones.stream()
                .filter(f -> f.getSala().getId() == idSala).toList(); }
    }

    @SuppressWarnings("unchecked")
    private static <T> T daoLectura(Class<T> tipo, Map<Integer, ?> elementos) {
        return (T) Proxy.newProxyInstance(
                FuncionBLImplTest.class.getClassLoader(),
                new Class<?>[]{tipo},
                (proxy, metodo, argumentos) -> switch (metodo.getName()) {
                    case "findById", "findByIdCompleto" -> elementos.get((Integer) argumentos[0]);
                    case "findByIds" -> ((Collection<Integer>) argumentos[0]).stream()
                            .map(elementos::get).filter(java.util.Objects::nonNull).toList();
                    case "findAll" -> List.copyOf(elementos.values());
                    case "toString" -> "DAO de lectura falso para " + tipo.getSimpleName();
                    default -> valorPredeterminado(metodo.getReturnType());
                });
    }

    private static Object valorPredeterminado(Class<?> tipo) throws SQLException {
        if (List.class.isAssignableFrom(tipo)) {
            return List.of();
        }
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
}
