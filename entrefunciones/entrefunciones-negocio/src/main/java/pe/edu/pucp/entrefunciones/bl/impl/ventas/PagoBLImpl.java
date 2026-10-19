package pe.edu.pucp.entrefunciones.bl.impl.ventas;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.impl.TransaccionBL;
import pe.edu.pucp.entrefunciones.bl.ventas.PagoBL;
import pe.edu.pucp.entrefunciones.dao.ventas.EntradaDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.PagoDAO;
import pe.edu.pucp.entrefunciones.dao.ventas.ReservaDAO;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

/**
 * Orquesta PagoDAO con ReservaDAO y EntradaDAO para el flujo de RF-M5-02 +
 * RF-M5-03 (ver el javadoc de PagoBL). Cada metodo publico atrapa
 * SQLException y la envuelve en BLException, y aplica primero la regla de
 * negocio correspondiente sobre el objeto de dominio (que ya la conoce:
 * Pago.procesarPago/validar/rechazar) antes de persistir — la capa de
 * negocio no reimplementa esas reglas, solo decide que hacer cuando el
 * objeto de dominio dice que la operacion no procede.
 *
 * <p>Todos los metodos que escriben (insert/update/delete/generarPago/
 * procesarPago/validar/rechazar) estan envueltos en
 * {@link TransaccionBL#ejecutar}, siguiendo el mismo patron que ya usa el
 * paquete "espacios" (Sede/Bloqueo/Butaca) y que sigue el patron del
 * profesor (la BL es quien inicia/confirma/revierte la transaccion). Esto
 * cierra el hueco de atomicidad mas importante del proyecto:
 * {@link #validar(int)} hace tres escrituras (pago, reserva y una entrada
 * por butaca) que antes corrian cada una en su propia conexion
 * auto-commit — si la emision de la segunda entrada fallaba, el pago y la
 * reserva ya habian quedado confirmados igual, con solo una entrada
 * emitida. Ahora, si cualquier paso falla, TransactionsManager revierte
 * las tres escrituras completas.</p>
 */
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
        TransaccionBL.ejecutar("Error de base de datos al registrar el pago", () -> {
            try {
                pagoDAO.insert(pago);
            } catch (SQLException ex) {
                throw new BLException("Error de base de datos al registrar el pago", ex);
            }
        });
    }

    @Override
    public void update(Pago pago) throws BLException {
        TransaccionBL.ejecutar("Error de base de datos al actualizar el pago", () -> {
            try {
                pagoDAO.update(pago);
            } catch (SQLException ex) {
                throw new BLException("Error de base de datos al actualizar el pago", ex);
            }
        });
    }

    @Override
    public void delete(Integer id) throws BLException {
        TransaccionBL.ejecutar("Error de base de datos al rechazar el pago " + id, () -> {
            try {
                pagoDAO.delete(id);
            } catch (SQLException ex) {
                throw new BLException("Error de base de datos al rechazar el pago " + id, ex);
            }
        });
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

        Pago[] generado = new Pago[1];
        TransaccionBL.ejecutar(
                "Error de base de datos al generar el pago de la reserva " + reserva.getId(), () -> {
                    try {
                        if (pagoDAO.findByReserva(reserva.getId()) != null) {
                            throw new BLException(
                                    "La reserva " + reserva.getId() + " ya tiene un pago generado");
                        }

                        String codigoPago = "PAY-" + String.format("%06d", reserva.getId())
                                + "-" + (System.currentTimeMillis() % 100000);
                        Pago pago = new Pago(0, codigoPago, monto, reserva);
                        pagoDAO.insert(pago);
                        generado[0] = pago;
                    } catch (SQLException ex) {
                        throw new BLException(
                                "Error de base de datos al generar el pago de la reserva "
                                        + reserva.getId(), ex);
                    }
                });
        return generado[0];
    }

    @Override
    public void procesarPago(int pagoId, double montoRecibido) throws BLException {
        TransaccionBL.ejecutar("Error de base de datos al procesar el pago " + pagoId, () -> {
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
        });
    }

    @Override
    public List<Entrada> validar(int pagoId) throws BLException {
        List<Entrada> resultado = new ArrayList<>();
        TransaccionBL.ejecutar("Error de base de datos al validar el pago " + pagoId, () -> {
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
                resultado.addAll(entradas);
            } catch (SQLException ex) {
                throw new BLException("Error de base de datos al validar el pago " + pagoId, ex);
            }
        });
        return resultado;
    }

    @Override
    public void rechazar(int pagoId) throws BLException {
        TransaccionBL.ejecutar("Error de base de datos al rechazar el pago " + pagoId, () -> {
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
        });
    }

    /**
     * RF-M5-03: una entrada por cupo reservado. El precio de cada entrada
     * es el precio del Evento (Funcion.getEvento().getPrecio()), y el
     * codigo QR se deriva del codigo de pago para que quede trazable de
     * que pago vino cada entrada emitida.
     *
     * <p>Se ramifica segun reserva.esAforoGeneral() (equivalente a
     * !reserva.getFuncion().getEvento().requiereAsientoNumerado()): en
     * asiento numerado (Pelicula,
     * ObraTeatro) se emite una entrada por butaca de reserva.getButacas();
     * en aforo general (ActividadCultural) no hay butacas que recorrer,
     * asi que se emiten reserva.getCantidad() entradas sin butaca
     * asignada, una por cupo comprado.</p>
     */
    private List<Entrada> emitirEntradas(Reserva reserva) {
        List<Entrada> entradas = new ArrayList<>();
        double precio = reserva.getFuncion().getEvento().getPrecio();
        LocalDateTime ahora = LocalDateTime.now();
        String codigoPago = reserva.getPago().getCodigoPago();

        if (reserva.esAforoGeneral()) {
            for (int i = 1; i <= reserva.getCantidad(); i++) {
                String qr = "QR-" + codigoPago + "-" + i;
                entradas.add(new Entrada(0, ahora, qr, precio, reserva));
            }
            return entradas;
        }

        int i = 1;
        for (Butaca butaca : reserva.getButacas()) {
            String qr = "QR-" + codigoPago + "-" + i;
            entradas.add(new Entrada(0, ahora, qr, precio, butaca, reserva));
            i++;
        }
        return entradas;
    }
}
