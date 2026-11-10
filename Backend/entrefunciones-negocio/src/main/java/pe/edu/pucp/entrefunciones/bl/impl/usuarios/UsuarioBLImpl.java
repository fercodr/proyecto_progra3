package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.dao.impl.usuarios.UsuarioDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.usuarios.UsuarioBL;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public abstract class UsuarioBLImpl<T extends Usuario>
        implements UsuarioBL<T> {
    // Cualquier correo (gmail, hotmail, pucp...): solo revisa que tenga forma de correo
    private static final Pattern CORREO =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int PASSWORD_MIN = 6;

    protected final UsuarioDAOImpl<T> usuarioDAO;

    protected UsuarioBLImpl(UsuarioDAOImpl<T> usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public List<T> findAll() throws BLException {
        try {
            return usuarioDAO.findAll();
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar los usuarios", e);
        }
    }

    @Override
    public T findById(Integer id) throws BLException {
        try {
            return usuarioDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar el usuario", e);
        }
    }

    @Override
    public void insert(T usuario) throws BLException {
        validarFormato(usuario);

        // Se guarda el hash, nunca la contraseña real
        usuario.setContrasena(PasswordUtil.hashear(usuario.getContrasena()));

        try {
            usuarioDAO.insert(usuario);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo registrar el usuario", e);
        }
    }

    @Override
    public void update(T usuario) throws BLException {
        validarExiste(usuario.getId());
        validarFormato(usuario);

        try {
            usuarioDAO.update(usuario);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo actualizar el usuario", e);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            usuarioDAO.delete(id);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo eliminar el usuario", e);
        }
    }

    @Override
    public T findByCorreo(String correo) throws BLException {
        try {
            return usuarioDAO.findByCorreo(correo);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el usuario por correo", e);
        }
    }

    @Override
    public T findByDni(String dni) throws BLException {
        try {
            return usuarioDAO.findByDni(dni);
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo buscar el usuario por DNI", e);
        }
    }

    protected void validarFormato(T usuario) throws BLException {

        if (usuario == null) {
            throw new BLException("El usuario no puede ser nulo");
        }

        if (usuario.getNombres() == null ||
                usuario.getNombres().trim().isEmpty()) {
            throw new BLException("Los nombres son obligatorios");
        }

        if (usuario.getApellidos() == null ||
                usuario.getApellidos().trim().isEmpty()) {
            throw new BLException("Los apellidos son obligatorios");
        }

        if (usuario.getCorreo() == null ||
                usuario.getCorreo().trim().isEmpty()) {
            throw new BLException("El correo es obligatorio");
        }

        if (usuario.getDni() == null ||
                usuario.getDni().trim().isEmpty()) {
            throw new BLException("El DNI es obligatorio");
        }

        if (usuario.getContrasena() == null ||
                usuario.getContrasena().trim().isEmpty()) {
            throw new BLException("La contraseña es obligatoria");
        }

        if (!CORREO.matcher(usuario.getCorreo().trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato válido");
        }

        // Se valida ANTES de hashear (en update ya es el hash y siempre pasa)
        if (usuario.getContrasena().length() < PASSWORD_MIN) {
            throw new ValidacionException(
                    "La contraseña debe tener al menos " + PASSWORD_MIN + " caracteres");
        }

        validarUnicos(usuario);
    }

    //Evita el error 500 de MySQL cuando el correo o DNI ya están registrados
    private void validarUnicos(T usuario) throws BLException {
        try {
            T otro = usuarioDAO.findByCorreo(usuario.getCorreo().trim());
            if (otro != null && otro.getId() != usuario.getId()) {
                throw new ValidacionException("Ya existe una cuenta con ese correo");
            }
            otro = usuarioDAO.findByDni(usuario.getDni().trim());
            if (otro != null && otro.getId() != usuario.getId()) {
                throw new ValidacionException("Ya existe una cuenta con ese DNI");
            }
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar el correo o DNI", e);
        }
    }

    protected void validarExiste(Integer id) throws BLException {
        try {
            if (usuarioDAO.findById(id) == null) {
                throw new BLException(
                        "No existe un usuario con id " + id);
            }
        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar la existencia del usuario", e);
        }
    }

}
