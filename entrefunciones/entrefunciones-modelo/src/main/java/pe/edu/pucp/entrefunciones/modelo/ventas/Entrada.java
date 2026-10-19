package pe.edu.pucp.entrefunciones.modelo.ventas;
import java.time.LocalDateTime;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
/**
 * Se emite una Entrada una vez que el Pago de la Reserva queda VALIDADO
 * (RF-M5-03): una por butaca reservada en asiento numerado (Pelicula,
 * ObraTeatro), o una por cupo en aforo general (ActividadCultural, sin
 * butaca asignada) — de ahi los dos constructores, igual que en Reserva.
 */
public class Entrada {
    private int id;
    private LocalDateTime fechaEmision;
    private String codigoQR;
    private double precioFinal;
    private EstadoEntrada estado;
    private Butaca butaca;
    private Reserva reserva;

    /** Entrada por asiento numerado: ligada a una butaca concreta. */
    public Entrada (int id, LocalDateTime fechaEmision, String codigoQR, double precioFinal, Butaca butaca, Reserva reserva){
        setId(id);
        setFechaEmision(fechaEmision);
        setCodigoQR(codigoQR);
        setPrecioFinal(precioFinal);
        setButaca(butaca);
        setReserva(reserva);
        this.estado=EstadoEntrada.VIGENTE;
    }

    /** Entrada por aforo general (ActividadCultural): sin butaca asignada. */
    public Entrada (int id, LocalDateTime fechaEmision, String codigoQR, double precioFinal, Reserva reserva){
        setId(id);
        setFechaEmision(fechaEmision);
        setCodigoQR(codigoQR);
        setPrecioFinal(precioFinal);
        this.butaca=null;
        setReserva(reserva);
        this.estado=EstadoEntrada.VIGENTE;
    }

    /** Constructor de copia. */
    public Entrada (final Entrada entrada){
        if(entrada == null){
            throw new IllegalArgumentException("entrada no puede ser nulo");
        }
        setId(entrada.getId());
        setFechaEmision(entrada.getFechaEmision());
        setCodigoQR(entrada.getCodigoQR());
        setPrecioFinal(entrada.getPrecioFinal());
        this.butaca=entrada.getButaca();
        setReserva(entrada.getReserva());
        setEstado(entrada.getEstado());
    }

    /** true si esta entrada es de aforo general (ActividadCultural, sin butaca asignada). */
    public boolean esAforoGeneral(){
        return butaca == null;
    }

    /** RF-M6-01: valida el ingreso del asistente; una entrada usada o cancelada no puede volver a validarse. */
    public boolean validarIngreso(){
        if(estado == EstadoEntrada.VIGENTE){
            estado = EstadoEntrada.UTILIZADA;
            return true;
        }
        return false;
    }

    /**
     * Eliminado logico de una entrada (RF-M5-03): no se borra del historial del
     * cliente, cambia de estado.
     */
    public boolean cancelar(){
        if(estado == EstadoEntrada.VIGENTE){
            estado = EstadoEntrada.CANCELADA;
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

    public LocalDateTime getFechaEmision(){
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision){
        if(fechaEmision == null){
            throw new IllegalArgumentException("fechaEmision no puede ser nulo");
        }
        this.fechaEmision=fechaEmision;
    }

    public String getCodigoQR(){
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR){
        if(codigoQR == null || codigoQR.isEmpty()){
            throw new IllegalArgumentException("codigoQR no puede ser nulo o vacio");
        }
        this.codigoQR=codigoQR;
    }

    public double getPrecioFinal(){
        return precioFinal;
    }

    public void setPrecioFinal(double precioFinal){
        if(precioFinal < 0){
            throw new IllegalArgumentException("precioFinal no puede ser negativo");
        }
        this.precioFinal=precioFinal;
    }

    public EstadoEntrada getEstado(){
        return estado;
    }

    public void setEstado(EstadoEntrada estado){
        if(estado == null){
            throw new IllegalArgumentException("estado no puede ser nulo");
        }
        this.estado=estado;
    }

    public Butaca getButaca(){
        return butaca;
    }

    public void setButaca(Butaca butaca){
        if(butaca == null){
            throw new IllegalArgumentException("butaca no puede ser nulo");
        }
        this.butaca=butaca;
    }

    public Reserva getReserva(){
        return reserva;
    }

    public void setReserva(Reserva reserva){
        if(reserva == null){
            throw new IllegalArgumentException("reserva no puede ser nulo");
        }
        this.reserva=reserva;
    }

}
