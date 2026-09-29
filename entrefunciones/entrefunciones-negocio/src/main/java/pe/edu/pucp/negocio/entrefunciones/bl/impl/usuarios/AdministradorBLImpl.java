package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.impl.usuarios.AdministradorDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.bl.usuarios.AdministradorBL;

public class AdministradorBLImpl
        extends UsuarioBLImpl<Administrador>
        implements AdministradorBL {

    public AdministradorBLImpl() {
        this.usuarioDAO = new AdministradorDAOImpl();
    }
}
