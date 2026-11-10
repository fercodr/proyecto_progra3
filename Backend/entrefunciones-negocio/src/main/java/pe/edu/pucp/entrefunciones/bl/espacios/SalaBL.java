package pe.edu.pucp.entrefunciones.bl.espacios;

import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;

import java.util.List;

public interface SalaBL extends BL<Sala, Integer> {
    List<Sala> listarPorSede(int idSede) throws BLException;
}
