 package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.impl.usuarios.ClienteDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.usuarios.ClienteBL;

import java.sql.SQLException;
import java.util.List;

public class ClienteBLImpl extends UsuarioBLImpl<Cliente> implements ClienteBL {

    private final ClienteDAOImpl clienteDAO;

    public ClienteBLImpl() {
        super(new ClienteDAOImpl());
        this.clienteDAO = (ClienteDAOImpl) super.usuarioDAO;
    }

    @Override
    public List<Cliente> findAll() throws BLException {
        try {
            return clienteDAO.findAll();
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar los clientes", e);
        }
    }

    @Override
    public Cliente findById(Integer id) throws BLException {
        try {
            return clienteDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar el cliente", e);
        }
    }

    @Override
    public void insert(Cliente cliente) throws BLException {
        validarFormato(cliente);

        try {
            clienteDAO.insert(cliente);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo registrar el cliente", e);
        }
    }

    @Override
    public void update(Cliente cliente) throws BLException {
        validarExiste(cliente.getId());
        validarFormato(cliente);

        try {
            clienteDAO.update(cliente);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo actualizar el cliente", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            clienteDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo eliminar el cliente", e);
        }
    }

    @Override
    public Cliente findByCorreo(String correo)
            throws BLException {
        try {
            return clienteDAO.findByCorreo(correo);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el cliente por correo", e);
        }
    }

    @Override
    public Cliente findByDni(String dni)
            throws BLException {
        try {
            return clienteDAO.findByDni(dni);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el cliente por DNI", e);
        }
    }

    @Override
    protected void validarFormato(Cliente cliente)
            throws BLException {

        super.validarFormato(cliente);

        if (cliente.getTelefono() == null ||
                cliente.getTelefono().trim().isEmpty()) {
            throw new BLException(
                    "El teléfono es obligatorio");
        }
    }
}
