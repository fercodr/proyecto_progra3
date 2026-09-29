package pe.edu.pucp.entrefunciones.bl.espacios;

import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import java.util.List;

public interface ButacaBL extends BL<Butaca, Integer> {
    List<Butaca> listarPorSala(int idSala) throws BLException;
}
