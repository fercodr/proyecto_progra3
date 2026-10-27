package pe.edu.pucp.entrefunciones.bl.usuarios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;

public interface AdministradorBL
        extends UsuarioBL<Administrador> {
    Administrador findByCodigoEmpleado(String codigoEmpleado) throws BLException;
}
