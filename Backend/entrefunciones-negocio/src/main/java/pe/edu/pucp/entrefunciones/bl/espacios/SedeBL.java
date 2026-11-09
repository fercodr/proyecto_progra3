package pe.edu.pucp.entrefunciones.bl.espacios;

import pe.edu.pucp.entrefunciones.bl.BL;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

public interface SedeBL extends BL<Sede, Integer> {
    Sede findByIdCompleto(Integer id) throws BLException;
    void sincronizarSalas(Sede sede) throws BLException;
}
