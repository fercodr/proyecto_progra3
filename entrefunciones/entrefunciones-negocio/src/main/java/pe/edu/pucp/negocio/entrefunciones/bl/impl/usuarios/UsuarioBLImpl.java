package pe.edu.pucp.negocio.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.negocio.entrefunciones.bl.usuarios.ClienteBL;

public class ClienteBLImpl extends UsuarioBLImpl<Cliente> implements ClienteBL {

    public ClienteBLImpl() {
        super(new ClienteDAOImpl());
    }
}
