package pe.edu.pucp.entrefunciones.bl.impl.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.dao.auditoria.AuditoriaDAO;
import pe.edu.pucp.entrefunciones.modelo.auditoria.Auditoria;
import pe.edu.pucp.entrefunciones.modelo.auditoria.EstadoAuditoria;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
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
        Clock reloj = Clock.fixed(
                Instant.parse("2026-11-09T15:30:00Z"),
                ZONA_LIMA);
        bl = new AuditoriaBLImpl(dao, reloj);

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
        dao.funcion = funcion;
        dao.administrador = administrador;
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

        assertThrows(
                BLException.class,
                () -> bl.registrar(
                        funcion.getId(),
                        administrador.getId(),
                        "Proyector sin imagen"));
    }

    @Test
    void registrarRechazaFuncionCancelada() {
        funcion.cancelar();

        assertThrows(
                BLException.class,
                () -> bl.registrar(
                        funcion.getId(),
                        administrador.getId(),
                        "Proyector sin imagen"));
    }

    @Test
    void actualizarAtencionConservaDatosOriginales() throws Exception {
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
        dao.administrador = nuevoResponsable;

        Auditoria actualizada = bl.actualizarAtencion(
                original.getId(),
                nuevoResponsable.getId(),
                EstadoAuditoria.RESUELTA);

        assertEquals(fechaOriginal, actualizada.getFecha());
        assertSame(funcion, actualizada.getFuncion());
        assertEquals("Descripcion original", actualizada.getDescripcion());
        assertSame(nuevoResponsable, actualizada.getResponsable());
        assertEquals(EstadoAuditoria.RESUELTA, actualizada.getEstado());
    }

    private static final class FakeAuditoriaDAO implements AuditoriaDAO {

        private Auditoria auditoria;
        private Funcion funcion;
        private Administrador administrador;

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

        @Override
        public List<Administrador> findResponsablesActivos() {
            return List.of(administrador);
        }

        @Override
        public List<Funcion> findFuncionesAsignables() {
            return List.of(funcion);
        }

        @Override
        public Administrador findResponsableById(Integer usuarioId) {
            return administrador != null && administrador.getId() == usuarioId
                    ? administrador
                    : null;
        }

        @Override
        public Funcion findFuncionById(Integer funcionId) {
            return funcion != null && funcion.getId() == funcionId
                    ? funcion
                    : null;
        }
    }
}
