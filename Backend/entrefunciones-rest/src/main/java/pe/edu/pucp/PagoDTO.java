package pe.edu.pucp.entrefunciones.rest.dto;

import java.time.LocalDateTime;

public class PagoDTO {
    private int id;
    private String codigoPago;
    private int reservaId;
    private double monto;
    private LocalDateTime fechaPago;
    private String estado; // PENDIENTE, VALIDADO, RECHAZADO

    public PagoDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoPago() {
        return codigoPago;
    }

    public void setCodigoPago(String codigoPago) {
        this.codigoPago = codigoPago;
    }

    public int getReservaId() {
        return reservaId;
    }

    public void setReservaId(int reservaId) {
        this.reservaId = reservaId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
