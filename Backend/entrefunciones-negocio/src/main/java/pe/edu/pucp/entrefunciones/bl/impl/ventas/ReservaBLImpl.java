package pe.edu.pucp.entrefunciones.bl.impl.ventas;
import pe.edu.pucp.entrefunciones.bl.*;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.ventas.ReservaBL;
import pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.ReservaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.ventas.EntradaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.ventas.*;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
public class ReservaBLImpl implements ReservaBL {
    private final ReservaDAOImpl reservas = new ReservaDAOImpl();
    private final EntradaDAOImpl entradas = new EntradaDAOImpl(
            new ReservaDAOImpl(), new ButacaDAOImpl());
    private final ControlOperacionesDAO control = new ControlOperacionesDAO();
    @FunctionalInterface private interface Consulta<T> { T ejecutar() throws SQLException; }
    private <T> T leer(Consulta<T> consulta) throws BLException {
        try { return consulta.ejecutar(); } catch(SQLException e) { throw new BLException("No se pudo consultar la reserva",e); }
    }
    @Override public List<Reserva> findAll() throws BLException { expirarVencidas(); return leer(reservas::findAll); }
    @Override public List<Reserva> listarPorCliente(int id) throws BLException { expirarVencidas(); return leer(()->reservas.findByClienteId(id)); }
    @Override public List<Reserva> listarPorFuncion(int id) throws BLException { expirarVencidas(); return leer(()->reservas.findByFuncionId(id)); }
    @Override public Reserva findById(Integer id) throws BLException { expirarVencidas(); return leer(()->reservas.findById(id)); }
    @Override public void insert(Reserva reserva) throws BLException { crearReserva(reserva); }
    @Override public void crearReserva(Reserva reserva) throws BLException {
        if(reserva==null) throw new BLException("La reserva no puede ser nula");
        Transacciones.ejecutar("No se pudo crear la reserva", () -> {
            control.bloquearFuncion(reserva.getFuncion().getId());
            var funcion = new FuncionDAOImpl().findById(reserva.getFuncion().getId());
            LocalDateTime ahora = control.ahora();
            if(funcion.getEstado()!=EstadoFuncion.CONFIRMADA || !funcion.getEvento().estaActivo()
                    || !funcion.getSala().estaActivo() || !funcion.getSala().getSede().estaActivo()
                    || !funcion.getFecha().atTime(funcion.getHoraInicio()).isAfter(ahora))
                throw new BLException("La función no está disponible para venta");
            var cliente = new ClienteDAOImpl().findById(reserva.getCliente().getId());
            if(cliente==null || !cliente.estaActivo()) throw new BLException("El cliente no está activo");
            control.expirarEnFuncion(funcion.getId());
            if(funcion.requiereAsientoNumerado()) {
                if(reserva.getButacas().isEmpty()) throw new BLException("Seleccione al menos una butaca");
                Set<Integer> elegidas = new HashSet<>();
                Set<Integer> ocupadas = control.butacasOcupadas(funcion.getId());
                ArrayList<Butaca> verificadas = new ArrayList<>();
                for(Butaca elegida:reserva.getButacas()) {
                    if(elegida==null || !elegidas.add(elegida.getId())) throw new BLException("Hay butacas repetidas");
                    Butaca butaca = new ButacaDAOImpl().findById(elegida.getId());
                    if(butaca==null || !butaca.estaActivo() || butaca.getSala().getId()!=funcion.getSala().getId())
                        throw new BLException("La butaca no pertenece a la sala o está inactiva");
                    if(ocupadas.contains(butaca.getId())) throw new BLException("Una butaca elegida ya está ocupada");
                    verificadas.add(butaca);
                }
                if(ocupadas.size()+verificadas.size()>funcion.getSala().getAforo()) throw new BLException("Se superó el aforo");
                reserva.setButacas(verificadas);
            } else {
                if(!reserva.getButacas().isEmpty()) throw new BLException("Aforo general no admite butacas");
                if(reserva.getCantidad()<=0 || reserva.getCantidad()>funcion.getSala().getAforo()-control.cuposOcupados(funcion.getId()))
                    throw new BLException("No hay cupos suficientes");
            }
            reserva.setFuncion(funcion); reserva.setCliente(cliente);
            reserva.setEstado(EstadoReserva.ACTIVA);
            reserva.setFechaCreacion(ahora);
            var inicio = funcion.getFecha().atTime(funcion.getHoraInicio());
            reserva.setFechaExpiracion(ahora.plusMinutes(15).isBefore(inicio)?ahora.plusMinutes(15):inicio);
            reserva.setPrecioUnitario(funcion.getEvento().getPrecio());
            reservas.insertConButacas(reserva,TransactionsManager.getConnection());
            if (Double.compare(reserva.getPrecioUnitario(), 0d) == 0) {
                if (!reserva.confirmar()) {
                    throw new BLException("La reserva gratuita no pudo confirmarse");
                }
                reservas.update(reserva);
                for (int i = 0; i < reserva.getCantidad(); i++) {
                    Butaca butaca = funcion.requiereAsientoNumerado()
                            ? reserva.getButacas().get(i)
                            : null;
                    Entrada entrada = new Entrada(
                            0,
                            ahora,
                            "QR-" + UUID.randomUUID(),
                            0d,
                            butaca,
                            reserva);
                    entradas.insert(entrada);
                }
            }
            return null;
        });
    }
    @Override public void update(Reserva reserva) throws BLException {
        if(reserva==null || reserva.getEstado()!=EstadoReserva.CANCELADA)
            throw new BLException("Use crearReserva o el flujo de pago; solo se permite cancelar una reserva");
        delete(reserva.getId());
    }
    @Override public void delete(Integer id) throws BLException {
        Transacciones.ejecutar("No se pudo cancelar la reserva", () -> {
            int funcion = control.funcionDeReserva(id); control.bloquearFuncion(funcion);control.bloquearReserva(id);
            control.expirarEnFuncion(funcion);
            Reserva reserva=reservas.findById(id);
            if(reserva.getEstado()==EstadoReserva.CANCELADA || reserva.getEstado()==EstadoReserva.EXPIRADA) return null;
            if(!reserva.cancelar()) throw new BLException("No se puede cancelar una reserva confirmada desde este flujo");
            reservas.update(reserva);control.expirarEnFuncion(funcion);return null;
        });
    }
    /** Se ejecuta también desde el temporizador del servidor, sin depender del navegador. */
    public void expirarVencidas() throws BLException {
        try {
            for(int id:control.funcionesPorExpirar()) Transacciones.ejecutar("No se pudo liberar el aforo vencido",()-> {
                control.bloquearFuncion(id);control.expirarEnFuncion(id);return null;
            });
        } catch(SQLException e) { throw new BLException("No se pudo consultar las reservas vencidas",e); }
    }
}
