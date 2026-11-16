package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.modelo.espacios.BloqueoSala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

class BloqueoSalaBLImplTest {
    @Test
    void rechazaFinNoPosteriorAntesDeConsultarLaBase() {
        BloqueoSala bloqueo = bloqueo(
                LocalDateTime.of(2026, 11, 18, 12, 0),
                LocalDateTime.of(2026, 11, 18, 11, 0));

        assertThrows(ValidacionException.class,
                () -> new BloqueoSalaBLImpl().insert(bloqueo));
    }

    @Test
    void desactivacionConservaSalaYHorarioHistorico() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 18, 12, 0);
        BloqueoSala bloqueo = bloqueo(inicio, inicio.plusHours(2));

        bloqueo.desactivar();

        assertFalse(bloqueo.estaActivo());
        assertEquals(1, bloqueo.getSala().getId());
        assertEquals(inicio, bloqueo.getInicio());
        assertEquals(inicio.plusHours(2), bloqueo.getFin());
    }

    private BloqueoSala bloqueo(LocalDateTime inicio, LocalDateTime fin) {
        Sede sede = new Sede(1, "Centro", "Av. Uno", "Lima", "014000000");
        Sala sala = new Sala(1, sede, "Sala 1", 100, "Teatro");
        return new BloqueoSala(0, sala, inicio, fin);
    }
}
