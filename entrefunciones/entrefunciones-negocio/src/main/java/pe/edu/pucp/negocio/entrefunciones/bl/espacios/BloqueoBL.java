package pe.edu.pucp.entrefunciones.bl.espacios;

import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import java.util.List;

public interface BloqueoBL extends BL<Bloqueo, Integer> {
    List<Bloqueo> listarPorSala(int idSala) throws BLException;
    List<Bloqueo> listarPorRecurso(int idRecurso) throws BLException;
}
