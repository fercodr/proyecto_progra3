package pe.edu.pucp.negocio.entrefunciones.bl.ventas;
import java.util.List;
import pe.edu.pucp.negocio.entrefunciones.bl.BL;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;
import pe.edu.pucp.entrefunciones.modelo.ventas.Pago;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;

/**
 * Reglas de negocio de Pago (RF-M5-02). Ademas del CRUD generico de
 * BL&lt;Pago, Integer&gt;, agrega el flujo completo descrito en el
 * catalogo de requerimientos y en el propio javadoc de Pago/Reserva:
 * <ol>
 *   <li>{@link #generarPago}: el sistema genera el codigo de pago para
 *   una reserva (una sola vez por reserva, ver uk_pago_reserva);</li>
 *   <li>{@link #procesarPago}: el cliente registra el pago realizado;</li>
 *   <li>{@link #validar}: un administrador valida el codigo para
 *   confirmar la operacion — esto tambien confirma la Reserva asociada
 *   (ver el javadoc de {@code Reserva.confirmar()}: "se llama cuando el
 *   Pago asociado queda VALIDADO") y emite sus entradas (RF-M5-03), por
 *   eso devuelve la lista de entradas emitidas;</li>
 *   <li>{@link #rechazar}: un administrador (o el sistema) rechaza el
 *   pago; a proposito NO cancela la reserva por si sola — esa decision
 *   de negocio no esta documentada en el dominio actual y se deja a la
 *   parte 2 del equipo (Reserva), que es quien conoce el flujo completo
 *   de expiracion/cancelacion.</li>
 * </ol>
 */
public interface PagoBL extends BL<Pago, Integer> {
    Pago buscarPorReserva(int reservaId) throws BLException;
    Pago buscarPorCodigo(String codigoPago) throws BLException;
    Pago generarPago(Reserva reserva, double monto) throws BLException;
    void procesarPago(int pagoId, double montoRecibido) throws BLException;
    List<Entrada> validar(int pagoId) throws BLException;
    void rechazar(int pagoId) throws BLException;
}
