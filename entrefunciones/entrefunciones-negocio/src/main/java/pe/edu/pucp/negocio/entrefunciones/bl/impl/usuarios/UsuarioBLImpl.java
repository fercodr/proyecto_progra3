 package pe.edu.pucp.negocio.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.usuarios.UsuarioDAO;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;
import pe.edu.pucp.negocio.entrefunciones.bl.usuarios.UsuarioBL;

import java.sql.SQLException;
import java.util.List;

public abstract class UsuarioBLImpl<T extends Usuario> implements UsuarioBL<T> {

    protected final UsuarioDAO<T> usuarioDAO;

    protected UsuarioBLImpl(UsuarioDAO<T> usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public List<T> findAll() throws BLException {
        try {
            return usuarioDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudo listar los usuarios", e);
        }
    }

    @Override
    public T findById(Integer id) throws BLException {
        try {
            return usuarioDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar el usuario", e);
        }
    }

    @Override
    public void insert(T usuario) throws BLException {
        validarFormato(usuario);
        try {
            usuarioDAO.insert(usuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo registrar el usuario", e);
        }
    }

    @Override
    public void update(T usuario) throws BLException {
        validarFormato(usuario);
        validarExiste(usuario.getId());
        try {
            usuarioDAO.update(usuario);
        } catch (SQLException e) {
            throw new BLException("No se pudo actualizar el usuario", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            usuarioDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo eliminar el usuario", e);
        }
    }

    @Override
    public T findByCorreo(String correo) throws BLException {
        try {
            return usuarioDAO.findByCorreo(correo);
        } catch (SQLException e) {
            throw new BLException("No se pudo buscar el usuario por correo", e);
        }
    }

    @Override
    public T findByDni(String dni) throws BLException {
        try {
            return usuarioDAO.findByDni(dni);
        } catch (SQLException e) {
            throw new BLException("No se pudo buscar el usuario por DNI", e);
        }
    }

    protected void validarFormato(T usuario) throws BLException {
        if (usuario == null) {
            throw new BLException("El usuario no puede ser nulo");
        }
        if (usuario.getNombres() == null || usuario.getNombres().trim().isEmpty()) {
            throw new BLException("Los nombres son obligatorios");
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().trim().isEmpty()) {
            throw new BLException("Los apellidos son obligatorios");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            throw new BLException("El correo es obligatorio");
        }
        if (usuario.getDni() == null || usuario.getDni().trim().isEmpty()) {
            throw new BLException("El DNI es obligatorio");
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            throw new BLException("La contraseña es obligatoria");
        }
    }

    protected void validarExiste(Integer id) throws BLException {
        try {
            if (usuarioDAO.findById(id) == null) {
                throw new BLException("No existe un usuario con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia del usuario", e);
        }
    }
}
