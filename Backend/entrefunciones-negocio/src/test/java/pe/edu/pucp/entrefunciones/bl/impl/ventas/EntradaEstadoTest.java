package pe.edu.pucp.entrefunciones.bl.impl.ventas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.modelo.catalogo.CategoriaGenero;
import pe.edu.pucp.entrefunciones.modelo.catalogo.ClasificacionEdad;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Pelicula;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.EstadoEntrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

class EntradaEstadoTest {

    @Test
    void cancelarUnaEntradaConservaIdentidadYNoCambiaLaOtra() {
        Reserva reserva = reservaConDosButacas();
        Entrada primera = new Entrada(
                101, LocalDateTime.of(2026, 11, 15, 10, 0),
                "QR-UNO", 20, reserva.getButacas().get(0), reserva);
        Entrada segunda = new Entrada(
                102, LocalDateTime.of(2026, 11, 15, 10, 0),
                "QR-DOS", 20, reserva.getButacas().get(1), reserva);

        assertTrue(primera.cancelar());

        assertEquals(101, primera.getId());
        assertEquals("QR-UNO", primera.getCodigoQR());
        assertEquals(EstadoEntrada.CANCELADA, primera.getEstado());
        assertEquals(EstadoEntrada.VIGENTE, segunda.getEstado());
        assertEquals("QR-DOS", segunda.getCodigoQR());
    }

    @Test
    void unaEntradaUtilizadaNoPuedeCancelarse() {
        Reserva reserva = reservaConDosButacas();
        Entrada entrada = new Entrada(
                101, LocalDateTime.of(2026, 11, 15, 10, 0),
                "QR-UNO", 20, reserva.getButacas().get(0), reserva);
        assertTrue(entrada.validarIngreso());

        assertFalse(entrada.cancelar());
        assertEquals(EstadoEntrada.UTILIZADA, entrada.getEstado());
    }

    private Reserva reservaConDosButacas() {
        Sede sede = new Sede(1, "Centro", "Av. Uno", "Lima", "014000000");
        Sala sala = new Sala(1, sede, "Sala 1", 20, "Cine 2D");
        Pelicula evento = new Pelicula(
                1, 20, "Pelicula", 100, CategoriaGenero.DRAMA,
                ClasificacionEdad.PG13, "Directora");
        Funcion funcion = new Funcion(
                1, evento, sala, LocalDate.of(2026, 11, 20),
                LocalTime.of(18, 0), LocalTime.of(20, 0), "Espanol");
        ArrayList<Butaca> butacas = new ArrayList<>();
        butacas.add(new Butaca(1, sala, "A", 1, "Regular"));
        butacas.add(new Butaca(2, sala, "A", 2, "Regular"));
        Cliente cliente = new Cliente(
                3, "Cliente", "Prueba", "cliente@prueba.pe", "hash",
                "12345678", LocalDate.of(2026, 1, 1), "900000000");
        return new Reserva(
                5, funcion, cliente, butacas,
                LocalDateTime.of(2026, 11, 15, 10, 0),
                LocalDateTime.of(2026, 11, 15, 10, 15));
    }
}
