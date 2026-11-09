package pe.edu.pucp.entrefunciones.bl.ventas;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;

/**
 * Reglas de negocio de Entrada. La emision de entradas ocurre como parte
 * de {@link PagoBL#validar} (una entrada nunca se emite por su cuenta:
 * siempre nace de un pago validado, ver el javadoc de la clase Entrada),
 * asi que esta interfaz cubre lo que pasa despues de emitida:
 * {@link #validarIngreso} (RF-M6-01, el asistente presenta su QR en la
 * puerta) y {@link #cancelar} (eliminado logico, RF-M5-03).
 */
public interface EntradaBL extends BL<Entrada, Integer> {
    List<Entrada> buscarPorReserva(int reservaId) throws BLException;
    Entrada buscarPorCodigoQR(String codigoQR) throws BLException;
    void validarIngreso(String codigoQR) throws BLException;
    void cancelar(int entradaId) throws BLException;
}
