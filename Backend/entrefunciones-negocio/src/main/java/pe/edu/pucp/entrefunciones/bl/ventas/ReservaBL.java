package pe.edu.pucp.entrefunciones.bl.ventas;

import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.modelo.ventas.Reserva;
import java.util.List;

public interface ReservaBL extends BL<Reserva, Integer> {
    List<Reserva> listarPorCliente(int idCliente) throws BLException;
    List<Reserva> listarPorFuncion(int idFuncion) throws BLException;

    void crearReserva(Reserva reserva) throws BLException;
}
