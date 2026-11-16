package pe.edu.pucp.entrefunciones.bl.impl.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.dao.usuarios.AdministradorDAO;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

class AuditoriaBLImplTest {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private FakeAuditoriaDAO dao;
    private AuditoriaBLImpl bl;
    private Funcion funcion;
    private Administrador administrador;

    @BeforeEach
    void preparar() {
        dao = new FakeAuditoriaDAO();
        Sede sede = new Sede(
                1, "Centro", "Av. Principal 100", "Lima", "014000000");
        Sala sala = new Sala(1, sede, "Sala 1", 80, "Cine 2D");
        Pelicula pelicula = new Pelicula(
                1,
                25.0,
                "Pelicula A",
                120,
                CategoriaGenero.DRAMA,
                ClasificacionEdad.PG13,
                "Directora");
        funcion = new Funcion(
                10,
                pelicula,
                sala,
                LocalDate.of(2026, 11, 10),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                "Espanol");
        administrador = new Administrador(
                7, "Ana", "Perez", "EMP-0007", true);

        Clock reloj = Clock.fixed(
                Instant.parse("2026-11-09T15:30:00Z"), ZONA_LIMA);
        bl = new AuditoriaBLImpl(
                dao,
                crearFuncionDAOSimulado(),
                crearAdministradorDAOSimulado(),
                reloj);
    }

    @Test
    void registrarAsignaFechaYEstadoEnElServidor() throws Exception {
        Auditoria registrada = bl.registrar(
                funcion.getId(),
                administrador.getId(),
                "Proyector sin imagen");

        assertEquals(1, registrada.getId());
        assertEquals(
                LocalDateTime.of(2026, 11, 9, 10, 30),
                registrada.getFecha());
        assertEquals(EstadoAuditoria.PENDIENTE, registrada.getEstado());
        assertSame(funcion, registrada.getFuncion());
        assertSame(administrador, registrada.getResponsable());
    }

    @Test
    void registrarRechazaAdministradorInactivo() {
        administrador.desactivar();

        ValidacionException error = assertThrows(
                ValidacionException.class,
                () -> bl.registrar(
                        funcion.getId(),
                        administrador.getId(),
                        "Proyector sin imagen"));

        assertEquals(
                "El responsable no existe o no esta activo",
                error.getMessage());
    }

    @Test
    void registrarRechazaFuncionCancelada() {
        funcion.cancelar();

        ValidacionException error = assertThrows(
                ValidacionException.class,
                () -> bl.registrar(
                        funcion.getId(),
                        administrador.getId(),
                        "Proyector sin imagen"));

        assertEquals(
                "La funcion no existe o no es asignable",
                error.getMessage());
    }

    @Test
    void actualizarAtencionEditaDescripcionYConservaDatosHistoricos() throws Exception {
        LocalDateTime fechaOriginal = LocalDateTime.of(2026, 11, 8, 9, 0);
        Auditoria original = new Auditoria(
                25,
                fechaOriginal,
                administrador,
                "Descripcion original",
                funcion);
        dao.auditoria = original;
        Administrador nuevoResponsable = new Administrador(
                8, "Luis", "Torres", "EMP-0008", true);
        administrador = nuevoResponsable;

        Auditoria actualizada = bl.actualizarAtencion(
                original.getId(),
                nuevoResponsable.getId(),
                "Descripcion corregida",
                EstadoAuditoria.RESUELTA);

        assertEquals(fechaOriginal, actualizada.getFecha());
        assertSame(funcion, actualizada.getFuncion());
        assertEquals("Descripcion corregida", actualizada.getDescripcion());
        assertSame(nuevoResponsable, actualizada.getResponsable());
        assertEquals(EstadoAuditoria.RESUELTA, actualizada.getEstado());
    }

    @Test
    void actualizarAtencionRechazaDescripcionVacia() {
        ValidacionException error = assertThrows(
                ValidacionException.class,
                () -> bl.actualizarAtencion(
                        25,
                        administrador.getId(),
                        "  ",
                        EstadoAuditoria.EN_PROCESO));

        assertEquals("La descripcion es obligatoria", error.getMessage());
    }

    private FuncionDAO crearFuncionDAOSimulado() {
        return (FuncionDAO) Proxy.newProxyInstance(
                FuncionDAO.class.getClassLoader(),
                new Class<?>[]{FuncionDAO.class},
                (proxy, metodo, argumentos) -> {
                    if (metodo.getName().equals("findAllActiveFunctions")) {
                        return funcion.getEstado() == EstadoFuncion.CANCELADA
                                ? List.of()
                                : List.of(funcion);
                    }
                    throw new UnsupportedOperationException(
                            "Metodo no simulado: " + metodo.getName());
                });
    }

    private AdministradorDAO crearAdministradorDAOSimulado() {
        return (AdministradorDAO) Proxy.newProxyInstance(
                AdministradorDAO.class.getClassLoader(),
                new Class<?>[]{AdministradorDAO.class},
                (proxy, metodo, argumentos) -> {
                    if (metodo.getName().equals("findAllActiveAdmins")) {
                        return administrador.estaActivo()
                                ? List.of(administrador)
                                : List.of();
                    }
                    throw new UnsupportedOperationException(
                            "Metodo no simulado: " + metodo.getName());
                });
    }

    private static final class FakeAuditoriaDAO implements AuditoriaDAO {

        private Auditoria auditoria;

        @Override
        public List<Auditoria> findAll() {
            List<Auditoria> resultado = new ArrayList<>();
            if (auditoria != null) {
                resultado.add(auditoria);
            }
            return resultado;
        }

        @Override
        public Auditoria findById(Integer id) {
            if (auditoria == null || auditoria.getId() != id) {
                return null;
            }
            return auditoria;
        }

        @Override
        public List<Auditoria> findByFilters(
                String descripcion, EstadoAuditoria estado) {
            return findAll();
        }

        @Override
        public void insert(Auditoria modelo) {
            modelo.setId(1);
            auditoria = modelo;
        }

        @Override
        public void update(Auditoria modelo) {
            auditoria = modelo;
        }

        @Override
        public void delete(Integer id) throws SQLException {
            throw new SQLException("No soportado");
        }

    }
}
