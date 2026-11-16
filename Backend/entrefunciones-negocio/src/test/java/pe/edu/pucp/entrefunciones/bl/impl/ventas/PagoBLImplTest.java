package pe.edu.pucp.entrefunciones.bl.impl.ventas;

import org.junit.jupiter.api.Test;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.dao.ventas.EntradaDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.PagoDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagoBLImplTest {

    @Test
    void buscarPorCodigoNormalizaElCodigoYConsultaElDao() throws Exception {
        PagoDAOFalso pagoDAO = new PagoDAOFalso();
        PagoBLImpl bl = new PagoBLImpl(pagoDAO, new ReservaDAOFalso(), new EntradaDAOFalso());

        bl.buscarPorCodigo("  PAY-LAB12-001  ");

        assertEquals("PAY-LAB12-001", pagoDAO.codigoConsultado);
    }

    @Test
    void buscarPorCodigoRechazaUnCodigoVacio() {
        PagoBLImpl bl = new PagoBLImpl(
                new PagoDAOFalso(), new ReservaDAOFalso(), new EntradaDAOFalso());

        assertThrows(ValidacionException.class, () -> bl.buscarPorCodigo("   "));
    }

    private static final class PagoDAOFalso implements PagoDAO {
        private String codigoConsultado;
        @Override public Pago findByCodigoPago(String codigoPago) { codigoConsultado = codigoPago; return null; }
        @Override public Pago findByReserva(int reservaId) { return null; }
        @Override public List<Pago> findAll() { return List.of(); }
        @Override public Pago findById(Integer id) { return null; }
        @Override public void insert(Pago modelo) { }
        @Override public void update(Pago modelo) { }
        @Override public void delete(Integer id) { }
    }

    private static final class ReservaDAOFalso implements ReservaDAO {
        @Override public List<Reserva> findByClienteId(int clienteId) { return List.of(); }
        @Override public List<Reserva> findByFuncionId(int funcionId) { return List.of(); }
        @Override public void insertConButacas(Reserva reserva, Connection conn) { }
        @Override public List<Reserva> findAll() { return List.of(); }
        @Override public Reserva findById(Integer id) { return null; }
        @Override public void insert(Reserva modelo) { }
        @Override public void update(Reserva modelo) { }
        @Override public void delete(Integer id) { }
    }

    private static final class EntradaDAOFalso implements EntradaDAO {
        @Override public List<Entrada> findByReserva(int reservaId) { return List.of(); }
        @Override public Entrada findByCodigoQR(String codigoQR) { return null; }
        @Override public List<Entrada> findAll() { return List.of(); }
        @Override public Entrada findById(Integer id) { return null; }
        @Override public void insert(Entrada modelo) { }
        @Override public void update(Entrada modelo) { }
        @Override public void delete(Integer id) { }
    }
}
