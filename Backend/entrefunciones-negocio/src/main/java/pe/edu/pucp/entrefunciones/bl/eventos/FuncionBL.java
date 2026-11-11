package pe.edu.pucp.entrefunciones.bl.eventos;

import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;

import java.time.LocalDate;
import java.util.List;

public interface FuncionBL extends BL<Funcion, Integer> {
    List<Funcion> findReportes(LocalDate desde, LocalDate hasta,
                               Integer idSede, Integer idSala)
            throws BLException;
}
