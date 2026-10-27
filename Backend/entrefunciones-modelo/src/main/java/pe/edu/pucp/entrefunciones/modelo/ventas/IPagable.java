package pe.edu.pucp.entrefunciones.modelo.ventas;
public interface IPagable{
    boolean procesarPago(double monto);
    void generarComprobante();
}
