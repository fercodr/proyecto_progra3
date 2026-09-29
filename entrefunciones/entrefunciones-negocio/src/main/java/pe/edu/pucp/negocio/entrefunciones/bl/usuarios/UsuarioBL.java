package pe.edu.pucp.negocio.entrefunciones.bl.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.negocio.entrefunciones.bl.BL;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;

public interface UsuarioBL<T extends Usuario> extends BL<T, Integer> {

    T findByCorreo(String correo) throws BLException;

    T findByDni(String dni) throws BLException;
}
