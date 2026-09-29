package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.bl.usuarios.ClienteBL;

public class ClienteBLImpl
        extends UsuarioBLImpl<Cliente>
        implements ClienteBL {

    public ClienteBLImpl() {
        this.usuarioDAO = new ClienteDAOImpl();
    }
}
