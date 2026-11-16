package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EventoBLImplTest {

    @Test
    void registraContenidoYProgramacionEnUnaSolaOperacionDelDao() throws Exception {
        EventoDAOFalso dao = new EventoDAOFalso();
        EventoBLImpl bl = new EventoBLImpl(dao);
        Pelicula evento = new Pelicula(
                0, 25, "Película LAB12", 120,
                CategoriaGenero.DRAMA, ClasificacionEdad.PG13, "Dirección LAB12");
        evento.setTematica("Integración");
        evento.setPublicoObjetivo("Mayores de 13 años");
        Sala sala = new Sala(1,
                new Sede(1, "Sede", "Dirección", "Lima", "999999999"),
                "Sala 1", 50, "Numerada");
        Funcion funcion = new Funcion(
                0, evento, sala, LocalDate.now().plusDays(1),
                LocalTime.of(18, 0), LocalTime.of(20, 0), "Español");
        evento.agregarFuncion(funcion);

        bl.insert(evento);

        assertEquals(1, dao.inserciones);
        assertEquals(1, dao.guardado.getFunciones().size());
        assertSame(dao.guardado, dao.guardado.getFunciones().get(0).getEvento());
        assertEquals("Integración", dao.guardado.getTematica());
    }

    private static final class EventoDAOFalso implements EventoDAO {
        private Evento guardado;
        private int inserciones;
        @Override public List<Evento> findAll() { return guardado == null ? List.of() : List.of(guardado); }
        @Override public Evento findById(Integer id) { return guardado != null && guardado.getId() == id ? guardado : null; }
        @Override public List<Evento> findByIds(Collection<Integer> ids) {
            return guardado != null && ids.contains(guardado.getId()) ? List.of(guardado) : List.of();
        }
        @Override public void insert(Evento evento) {
            inserciones++;
            evento.setId(77);
            guardado = evento;
        }
        @Override public void update(Evento evento) { guardado = evento; }
        @Override public void delete(Integer id) { }
    }
}
