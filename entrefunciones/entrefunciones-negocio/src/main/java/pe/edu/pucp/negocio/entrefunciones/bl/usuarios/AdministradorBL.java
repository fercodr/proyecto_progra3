package pe.edu.pucp.negocio.entrefunciones.bl.usuarios;

import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;

public interface AdministradorBL extends UsuarioBL<Administrador> {

    Administrador findByCodigoEmpleado(String codigoEmpleado) throws BLException;
}
