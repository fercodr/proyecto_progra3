package pe.edu.pucp.entrefunciones.modelo.ventas;
import java.util.ArrayList;
import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
/**
 * RF-M5-01: reserva temporal de una funcion para un cliente. Hay dos formas
 * de reservar, segun lo que pida Funcion.getEvento().requiereAsientoNumerado():
 * un conjunto de butacas
 * especificas (Pelicula, ObraTeatro), o una cantidad de cupos de aforo
 * general sin butaca puntual (ActividadCultural) — de ahi los dos
 * constructores. Mientras esta ACTIVA, esas butacas (o ese cupo) se
 * consideran ocupados para esa funcion (una butaca puede seguir libre en
 * otras funciones de la misma sala). Si vence, se cancela o no se completa
 * el pago, la reserva pasa a EXPIRADA/CANCELADA y el cupo queda libre otra
 * vez para esa funcion.
 */
public class Reserva {
    private int id;
    private Funcion funcion;
    private Cliente cliente;
    private ArrayList<Butaca> butacas;
    private int cantidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private EstadoReserva estado;
    private Pago pago;

    /** Reserva por asiento numerado (Pelicula, ObraTeatro): un grupo de butacas concretas. */
    public Reserva (int id, Funcion funcion, Cliente cliente, ArrayList<Butaca> butacas, LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion){
        if(fechaCreacion != null && fechaExpiracion != null && !fechaExpiracion.isAfter(fechaCreacion)){
            throw new IllegalArgumentException("fechaExpiracion debe ser posterior a fechaCreacion");
        }
        setId(id);
        setFuncion(funcion);
        if(!funcion.getEvento().requiereAsientoNumerado()){
            throw new IllegalArgumentException("esta funcion es de aforo general (ver Evento.requiereAsientoNumerado): usar el constructor con cantidad, no con butacas");
        }
        setCliente(cliente);
        setButacas(butacas);
        this.cantidad=butacas.size();
        setFechaCreacion(fechaCreacion);
        setFechaExpiracion(fechaExpiracion);
        this.estado=EstadoReserva.ACTIVA;
    }

    /** Reserva por aforo general (ActividadCultural): solo se cuenta un numero de cupos, sin butaca especifica. */
    public Reserva (int id, Funcion funcion, Cliente cliente, int cantidad, LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion){
        if(fechaCreacion != null && fechaExpiracion != null && !fechaExpiracion.isAfter(fechaCreacion)){
            throw new IllegalArgumentException("fechaExpiracion debe ser posterior a fechaCreacion");
        }
        setId(id);
        setFuncion(funcion);
        if(funcion.getEvento().requiereAsientoNumerado()){
            throw new IllegalArgumentException("esta funcion vende por asiento numerado (ver Evento.requiereAsientoNumerado): usar el constructor con butacas, no con cantidad");
        }
        setCliente(cliente);
        setCantidad(cantidad);
        this.butacas=new ArrayList<>();
        setFechaCreacion(fechaCreacion);
        setFechaExpiracion(fechaExpiracion);
        this.estado=EstadoReserva.ACTIVA;
    }

    //Constructor copia
    public Reserva (final Reserva reserva){
        if(reserva == null){
            throw new IllegalArgumentException("reserva no puede ser nulo");
        }
        if(reserva.getFechaCreacion() != null && reserva.getFechaExpiracion() != null
                && !reserva.getFechaExpiracion().isAfter(reserva.getFechaCreacion())){
            throw new IllegalArgumentException("fechaExpiracion debe ser posterior a fechaCreacion");
        }
        setId(reserva.getId());
        setFuncion(reserva.getFuncion());
        setCliente(reserva.getCliente());
        this.butacas=new ArrayList<>(reserva.getButacas());
        this.cantidad=reserva.getCantidad();
        setFechaCreacion(reserva.getFechaCreacion());
        setFechaExpiracion(reserva.getFechaExpiracion());
        setEstado(reserva.getEstado());
        this.pago=reserva.getPago();
    }

    /** true si esta reserva es de aforo general (ActividadCultural, sin butacas puntuales). */
    public boolean esAforoGeneral(){
        return butacas.isEmpty();
    }

    /** Se llama cuando el Pago asociado queda VALIDADO. */
    public boolean confirmar(){
        if(estado == EstadoReserva.ACTIVA){
            estado = EstadoReserva.CONFIRMADA;
            return true;
        }
        return false;
    }

    /** Eliminado logico: cancela la reserva y libera las butacas (o el cupo) para esa funcion. */
    public boolean cancelar(){
        if(estado == EstadoReserva.ACTIVA){
            estado = EstadoReserva.CANCELADA;
            return true;
        }
        return false;
    }

    /** RF-M5-01: libera automaticamente el cupo cuando la reserva vence. */
    public boolean expirarSiVencida(LocalDateTime ahora){
        if(estado == EstadoReserva.ACTIVA && ahora.isAfter(fechaExpiracion)){
            estado = EstadoReserva.EXPIRADA;
            return true;
        }
        return false;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        if(id < 0){
            throw new IllegalArgumentException("id no puede ser negativo");
        }
        this.id=id;
    }

    public Funcion getFuncion(){
        return funcion;
    }

    public void setFuncion(Funcion funcion){
        if(funcion == null){
            throw new IllegalArgumentException("funcion no puede ser nulo");
        }
        this.funcion=funcion;
    }

    public Cliente getCliente(){
        return cliente;
    }

    public void setCliente(Cliente cliente){
        if(cliente == null){
            throw new IllegalArgumentException("cliente no puede ser nulo");
        }
        this.cliente=cliente;
    }

    public ArrayList<Butaca> getButacas(){
        return butacas;
    }

    public void setButacas(ArrayList<Butaca> butacas){
        if(butacas == null || butacas.isEmpty()){
            throw new IllegalArgumentException("butacas no puede ser nulo ni vacio");
        }
        this.butacas=butacas;
    }

    /** Cupos que ocupa esta reserva: butacas.size() en asiento numerado, o el valor propio en aforo general. */
    public int getCantidad(){
        return cantidad;
    }

    public void setCantidad(int cantidad){
        if(cantidad <= 0){
            throw new IllegalArgumentException("cantidad debe ser mayor a cero");
        }
        this.cantidad=cantidad;
    }

    public LocalDateTime getFechaCreacion(){
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion){
        if(fechaCreacion == null){
            throw new IllegalArgumentException("fechaCreacion no puede ser nulo");
        }
        this.fechaCreacion=fechaCreacion;
    }

    public LocalDateTime getFechaExpiracion(){
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion){
        if(fechaExpiracion == null){
            throw new IllegalArgumentException("fechaExpiracion no puede ser nulo");
        }
        this.fechaExpiracion=fechaExpiracion;
    }

    public EstadoReserva getEstado(){
        return estado;
    }

    public void setEstado(EstadoReserva estado){
        if(estado == null){
            throw new IllegalArgumentException("estado no puede ser nulo");
        }
        this.estado=estado;
    }

    public Pago getPago(){
        return pago;
    }

    public void setPago(Pago pago){
        this.pago=pago;
    }

}
