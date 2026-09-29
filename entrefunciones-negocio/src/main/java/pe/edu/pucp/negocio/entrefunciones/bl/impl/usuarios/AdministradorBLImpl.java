package pe.edu.pucp.negocio.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.impl.usuarios.AdministradorDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;
import pe.edu.pucp.negocio.entrefunciones.bl.usuarios.AdministradorBL;

import java.sql.SQLException;
import java.util.List;

public class AdministradorBLImpl
        extends UsuarioBLImpl<Administrador>
        implements AdministradorBL {

    private final AdministradorDAOImpl administradorDAO;

    public AdministradorBLImpl() {
        super(new AdministradorDAOImpl());
        this.administradorDAO = (AdministradorDAOImpl) super.usuarioDAO;
    }

    @Override
    public List<Administrador> findAll() throws BLException {
        try {
            return administradorDAO.findAll();
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar los administradores", e);
        }
    }

    @Override
    public Administrador findById(Integer id) throws BLException {
        try {
            return administradorDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar el administrador", e);
        }
    }

    @Override
    public void insert(Administrador administrador) throws BLException {
        validarFormato(administrador);

        try {
            administradorDAO.insert(administrador);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo registrar el administrador", e);
        }
    }

    @Override
    public void update(Administrador administrador) throws BLException {
        validarExiste(administrador.getId());
        validarFormato(administrador);

        try {
            administradorDAO.update(administrador);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo actualizar el administrador", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            administradorDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo eliminar el administrador", e);
        }
    }

    @Override
    public Administrador findByCorreo(String correo)
            throws BLException {
        try {
            return administradorDAO.findByCorreo(correo);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el administrador por correo", e);
        }
    }

    @Override
    public Administrador findByDni(String dni)
            throws BLException {
        try {
            return administradorDAO.findByDni(dni);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el administrador por DNI", e);
        }
    }

    @Override
    public Administrador findByCodigoEmpleado(String codigoEmpleado)
            throws BLException {
        try {
            return administradorDAO.findByCodigoEmpleado(codigoEmpleado);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el administrador por código de empleado", e);
        }
    }

    @Override
    protected void validarFormato(Administrador administrador)
            throws BLException {

        super.validarFormato(administrador);

        if (administrador.getCodigoEmpleado() == null ||
                administrador.getCodigoEmpleado().trim().isEmpty()) {
            throw new BLException(
                    "El código de empleado es obligatorio");
        }
    }
}
