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

    public PagoBLImpl() {
        this(new pe.edu.pucp.entrefunciones.dao.impl.ventas.PagoDAOImpl(new pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl()),
             new pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl(),
             new pe.edu.pucp.entrefunciones.dao.impl.ventas.EntradaDAOImpl(new pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl(), new pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl()));
    }
    @Override public void insert(Pago pago) throws BLException { pago.setId(generarPago(pago.getReserva(),pago.getMonto()).getId()); }
    @Override public void update(Pago pago) throws BLException { throw new BLException("Use procesarPago, validar o rechazar"); }
    @Override public void delete(Integer id) throws BLException { rechazar(id); }

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


    private final pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO control = new pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO();
    private Reserva bloquearReserva(int id) throws SQLException, BLException {
        int funcion = control.funcionDeReserva(id);
        control.bloquearFuncion(funcion);control.bloquearReserva(id);
        control.expirarEnFuncion(funcion);
        return reservaDAO.findById(id);
    }
    private Pago bloquearPago(int id) throws SQLException, BLException {
        int reserva = control.reservaDePago(id);
        bloquearReserva(reserva);control.bloquearPago(id);
        return pagoDAO.findById(id);
    }
    private void exigirActiva(Reserva reserva) throws SQLException, BLException {
        if(reserva==null || reserva.getEstado()!=pe.edu.pucp.entrefunciones.modelo.ventas.EstadoReserva.ACTIVA
            || !control.ahora().isBefore(reserva.getFechaExpiracion())
            || reserva.getFuncion().getEstado()!=pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CONFIRMADA)
            throw new BLException("La reserva venció, fue cancelada o su función no está disponible");
    }
    @Override public Pago generarPago(Reserva solicitada, double montoIgnorado) throws BLException {
        if(solicitada==null) throw new BLException("Debe indicar una reserva");
        return pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo generar el pago",()-> {
            Reserva reserva=bloquearReserva(solicitada.getId());
            Pago existente=pagoDAO.findByReserva(reserva.getId());
            if(existente!=null && existente.getEstado()==pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago.VALIDADO) return existente;
            exigirActiva(reserva);
            if(existente!=null) return existente;
            Pago pago=new Pago(0,"PAY-"+java.util.UUID.randomUUID(),reserva.getMontoTotal(),reserva);
            pagoDAO.insert(pago);return pago;
        });
    }
    @Override public void procesarPago(int id,double monto) throws BLException {
        if(!Double.isFinite(monto) || monto<0) throw new BLException("Monto inválido");
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo registrar el pago",()-> {
            Pago pago=bloquearPago(id);
            if(pago.getEstado()==pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago.VALIDADO) return null;
            exigirActiva(pago.getReserva());
            if(pago.getEstado()!=pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago.PENDIENTE || monto<pago.getMonto()) throw new BLException("El pago está rechazado o el monto es insuficiente");
            if(pago.getFechaPago()==null) { pago.setFechaPago(control.ahora());pagoDAO.update(pago); }
            return null;
        });
    }
    @Override public List<Entrada> validar(int id) throws BLException {
        return pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo validar el pago y emitir entradas",()-> {
            Pago pago=bloquearPago(id);
            Reserva reserva=pago.getReserva();
            if(pago.getEstado()==pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago.VALIDADO)
                return entradaDAO.findByReserva(reserva.getId());
            exigirActiva(reserva);
            if(!pago.validar()) throw new BLException("El cliente todavía no ha registrado el pago o fue rechazado");
            if(!reserva.confirmar()) throw new BLException("La reserva no puede confirmarse");
            pagoDAO.update(pago);reservaDAO.update(reserva);
            List<Entrada> entradas=new ArrayList<>();
            LocalDateTime ahora=control.ahora();
            for(int i=0;i<reserva.getCantidad();i++) {
                Butaca butaca=reserva.getFuncion().requiereAsientoNumerado()?reserva.getButacas().get(i):null;
                Entrada entrada=new Entrada(0,ahora,"QR-"+java.util.UUID.randomUUID(),reserva.getPrecioUnitario(),butaca,reserva);
                entradaDAO.insert(entrada);entradas.add(entrada);
            }
            return entradas;
        });
    }
    @Override public void rechazar(int id) throws BLException {
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo rechazar el pago",()-> {
            Pago pago=bloquearPago(id);
            if(pago.getEstado()==pe.edu.pucp.entrefunciones.modelo.ventas.EstadoPago.RECHAZADO) return null;
            if(!pago.rechazar()) throw new BLException("El pago ya fue validado");
            pagoDAO.update(pago);
            Reserva reserva=pago.getReserva();
            if(reserva.cancelar()) reservaDAO.update(reserva);
            return null;
        });
    }
}
