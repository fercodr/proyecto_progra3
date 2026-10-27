package pe.edu.pucp.entrefunciones.bl.impl.ventas;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.ventas.PagoBL;
import pe.edu.pucp.entrefunciones.dao.ventas.EntradaDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.PagoDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;


public class PagoBLImpl implements PagoBL {

    private final PagoDAO pagoDAO;
    private final ReservaDAO reservaDAO;
    private final EntradaDAO entradaDAO;

    public PagoBLImpl(PagoDAO pagoDAO, ReservaDAO reservaDAO, EntradaDAO entradaDAO) {
        if (pagoDAO == null) {
            throw new IllegalArgumentException("El pagoDAO no puede ser nulo");
        }
        if (reservaDAO == null) {
            throw new IllegalArgumentException("El reservaDAO no puede ser nulo");
        }
        if (entradaDAO == null) {
            throw new IllegalArgumentException("El entradaDAO no puede ser nulo");
        }
        this.pagoDAO = pagoDAO;
        this.reservaDAO = reservaDAO;
        this.entradaDAO = entradaDAO;
    }

    @Override
    public List<Pago> findAll() throws BLException {
        try {
            return pagoDAO.findAll();
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al listar los pagos", ex);
        }
    }

    @Override
    public Pago findById(Integer id) throws BLException {
        try {
            return pagoDAO.findById(id);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al buscar el pago " + id, ex);
        }
    }

    @Override
    public void insert(Pago pago) throws BLException {
        try {
            pagoDAO.insert(pago);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al registrar el pago", ex);
        }
    }

    @Override
    public void update(Pago pago) throws BLException {
        try {
            pagoDAO.update(pago);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al actualizar el pago", ex);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            pagoDAO.delete(id);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al rechazar el pago " + id, ex);
        }
    }

    @Override
    public Pago buscarPorReserva(int reservaId) throws BLException {
        try {
            return pagoDAO.findByReserva(reservaId);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al buscar el pago de la reserva " + reservaId, ex);
        }
    }

    @Override
    public Pago buscarPorCodigo(String codigoPago) throws BLException {
        try {
            return pagoDAO.findByCodigoPago(codigoPago);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al buscar el pago " + codigoPago, ex);
        }
    }

    @Override
    public Pago generarPago(Reserva reserva, double monto) throws BLException {
        if (reserva == null) {
            throw new BLException("La reserva no puede ser nula");
        }

        try {
            if (pagoDAO.findByReserva(reserva.getId()) != null) {
                throw new BLException("La reserva " + reserva.getId() + " ya tiene un pago generado");
            }

            String codigoPago = "PAY-" + String.format("%06d", reserva.getId())
                    + "-" + (System.currentTimeMillis() % 100000);
            Pago pago = new Pago(0, codigoPago, monto, reserva);
            pagoDAO.insert(pago);
            return pago;
        } catch (SQLException ex) {
            throw new BLException(
                    "Error de base de datos al generar el pago de la reserva " + reserva.getId(), ex);
        }
    }

    @Override
    public void procesarPago(int pagoId, double montoRecibido) throws BLException {
        try {
            Pago pago = pagoDAO.findById(pagoId);
            if (pago == null) {
                throw new BLException("No existe el pago " + pagoId);
            }
            if (!pago.procesarPago(montoRecibido)) {
                throw new BLException(
                        "El pago " + pago.getCodigoPago() + " no se pudo procesar: ya no esta "
                                + "PENDIENTE, o el monto recibido (" + montoRecibido + ") es menor al esperado ("
                                + pago.getMonto() + ")");
            }
            pagoDAO.update(pago);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al procesar el pago " + pagoId, ex);
        }
    }

    @Override
    public List<Entrada> validar(int pagoId) throws BLException {
        try {
            Pago pago = pagoDAO.findById(pagoId);
            if (pago == null) {
                throw new BLException("No existe el pago " + pagoId);
            }
            if (!pago.validar()) {
                throw new BLException(
                        "El pago " + pago.getCodigoPago() + " no se pudo validar: todavia no esta "
                                + "PENDIENTE con un pago registrado (ver procesarPago)");
            }
            pagoDAO.update(pago);

            Reserva reserva = pago.getReserva();
            reserva.confirmar();
            reservaDAO.update(reserva);

            List<Entrada> entradas = emitirEntradas(reserva);
            for (Entrada entrada : entradas) {
                entradaDAO.insert(entrada);
            }
            return entradas;
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al validar el pago " + pagoId, ex);
        }
    }

    @Override
    public void rechazar(int pagoId) throws BLException {
        try {
            Pago pago = pagoDAO.findById(pagoId);
            if (pago == null) {
                throw new BLException("No existe el pago " + pagoId);
            }
            if (!pago.rechazar()) {
                throw new BLException(
                        "El pago " + pago.getCodigoPago() + " no se pudo rechazar: ya no esta PENDIENTE");
            }
            pagoDAO.update(pago);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al rechazar el pago " + pagoId, ex);
        }
    }


    private List<Entrada> emitirEntradas(Reserva reserva) {
        List<Entrada> entradas = new ArrayList<>();
        double precio = reserva.getFuncion().getEvento().getPrecio();
        LocalDateTime ahora = LocalDateTime.now();

        int i = 1;
        for (Butaca butaca : reserva.getButacas()) {
            String qr = "QR-" + reserva.getPago().getCodigoPago() + "-" + i;
            entradas.add(new Entrada(0, ahora, qr, precio, butaca, reserva));
            i++;
        }
        return entradas;
    }
}
