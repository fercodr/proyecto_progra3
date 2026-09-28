package pe.edu.pucp.entrefunciones.modelo.ventas;
import java.time.LocalDateTime;

/**
 * RF-M5-02: el sistema genera un codigo de pago para una Reserva, el cliente
 * registra el pago realizado (procesarPago) y un administrador valida el
 * codigo para confirmar la operacion (validar). Antes esta responsabilidad
 * estaba en Entrada; se separo porque el pago ocurre antes y puede quedar
 * PENDIENTE o RECHAZADO sin que llegue a emitirse ninguna entrada.
 */
public class Pago implements IPagable {
    private int id;
    private String codigoPago;
    private double monto;
    private LocalDateTime fechaPago;
    private EstadoPago estado;
    private Reserva reserva;

    public Pago (int id, String codigoPago, double monto, Reserva reserva){
        setId(id);
        setCodigoPago(codigoPago);
        setMonto(monto);
        setReserva(reserva);
        this.estado=EstadoPago.PENDIENTE;
        reserva.setPago(this); // mantiene consistente la referencia inversa Reserva -> Pago ("genera")
    }

    /** Constructor de copia. */
    public Pago (final Pago pago){
        if(pago == null){
            throw new IllegalArgumentException("pago no puede ser nulo");
        }
        setId(pago.getId());
        setCodigoPago(pago.getCodigoPago());
        setMonto(pago.getMonto());
        this.reserva=pago.getReserva();
        setEstado(pago.getEstado());
        this.fechaPago=pago.getFechaPago();
    }

    @Override
    public boolean procesarPago(double monto){
        if(estado == EstadoPago.PENDIENTE && monto >= this.monto){
            this.fechaPago = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /** El administrador valida el codigo de pago para confirmar la operacion (RF-M5-02). */
    public boolean validar(){
        if(estado == EstadoPago.PENDIENTE && fechaPago != null){
            estado = EstadoPago.VALIDADO;
            return true;
        }
        return false;
    }

    public boolean rechazar(){
        if(estado == EstadoPago.PENDIENTE){
            estado = EstadoPago.RECHAZADO;
            return true;
        }
        return false;
    }

    @Override
    public void generarComprobante(){
        System.out.println("Comprobante generado para el pago " + codigoPago + " por S/ " + String.format("%.2f",monto));
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

    public String getCodigoPago(){
        return codigoPago;
    }

    public void setCodigoPago(String codigoPago){
        if(codigoPago == null || codigoPago.isEmpty()){
            throw new IllegalArgumentException("codigoPago no puede ser nulo o vacio");
        }
        this.codigoPago=codigoPago;
    }

    public double getMonto(){
        return monto;
    }

    public void setMonto(double monto){
        if(monto < 0){
            throw new IllegalArgumentException("monto no puede ser negativo");
        }
        this.monto=monto;
    }

    public LocalDateTime getFechaPago(){
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago){
        this.fechaPago=fechaPago;
    }

    public EstadoPago getEstado(){
        return estado;
    }

    public void setEstado(EstadoPago estado){
        if(estado == null){
            throw new IllegalArgumentException("estado no puede ser nulo");
        }
        this.estado=estado;
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
