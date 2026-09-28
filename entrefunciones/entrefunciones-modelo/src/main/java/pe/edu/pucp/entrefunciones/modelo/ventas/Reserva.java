package pe.edu.pucp.entrefunciones.modelo.ventas;
import java.util.ArrayList;
import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
/**
 * RF-M5-01: reserva temporal de un conjunto de butacas de una funcion para un
 * cliente. Mientras esta ACTIVA, esas butacas se consideran ocupadas para esa
 * funcion (una butaca puede seguir libre en otras funciones de la misma
 * sala). Si vence, se cancela o no se completa el pago, la reserva pasa a
 * EXPIRADA/CANCELADA y las butacas quedan libres otra vez para esa funcion.
 */
public class Reserva {
    private int id;
    private Funcion funcion;
    private Cliente cliente;
    private ArrayList<Butaca> butacas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private EstadoReserva estado;
    private Pago pago;

    public Reserva (int id, Funcion funcion, Cliente cliente, ArrayList<Butaca> butacas, LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion){
        if(fechaCreacion != null && fechaExpiracion != null && !fechaExpiracion.isAfter(fechaCreacion)){
            throw new IllegalArgumentException("fechaExpiracion debe ser posterior a fechaCreacion");
        }
        setId(id);
        setFuncion(funcion);
        setCliente(cliente);
        setButacas(butacas);
        setFechaCreacion(fechaCreacion);
        setFechaExpiracion(fechaExpiracion);
        this.estado=EstadoReserva.ACTIVA;
    }

    //Constructor copia
    public Reserva (final Reserva reserva){
        if(reserva == null){
            throw new IllegalArgumentException("reserva no puede ser nulo");
        }
        setId(reserva.getId());
        setFuncion(reserva.getFuncion());
        setCliente(reserva.getCliente());
        setButacas(reserva.getButacas());
        setFechaCreacion(reserva.getFechaCreacion());
        setFechaExpiracion(reserva.getFechaExpiracion());
        setEstado(reserva.getEstado());
        this.pago=reserva.getPago();
    }

    /** Se llama cuando el Pago asociado queda VALIDADO. */
    public boolean confirmar(){
        if(estado == EstadoReserva.ACTIVA){
            estado = EstadoReserva.CONFIRMADA;
            return true;
        }
        return false;
    }

    /** Eliminado logico: cancela la reserva y libera las butacas para esa funcion. */
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
