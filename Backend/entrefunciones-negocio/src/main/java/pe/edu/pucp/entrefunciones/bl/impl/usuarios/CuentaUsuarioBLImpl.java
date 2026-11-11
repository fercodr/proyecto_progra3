package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.usuarios.AdministradorBL;
import pe.edu.pucp.entrefunciones.bl.usuarios.ClienteBL;
import pe.edu.pucp.entrefunciones.bl.usuarios.CuentaUsuarioBL;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Cliente;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

//no tiene dao propio-> de administrador y cliente
public class CuentaUsuarioBLImpl implements CuentaUsuarioBL {

    private final AdministradorBL administradorBL;
    private final ClienteBL clienteBL;

    public CuentaUsuarioBLImpl() {
        administradorBL = new AdministradorBLImpl();
        clienteBL = new ClienteBLImpl();
    }

    @Override
    public Usuario autenticar(String correo, String contrasena) throws BLException {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new ValidacionException("Ingrese correo y contraseña");
        }

        Usuario usuario = buscarPorCorreo(correo.trim());
        // así no se revela qué correos están registrados.
        if (usuario == null
                || !usuario.estaActivo()
                || !PasswordUtil.verificar(contrasena, usuario.getContrasena())) {
            return null;
        }
        return usuario;
    }

    @Override
    public Usuario findByCorreo(String correo) throws BLException {
        Usuario usuario = buscarPorCorreo(correo);
        if (usuario == null) {
            throw new NoEncontradoException("No existe una cuenta con el correo " + correo);
        }
        return usuario;
    }

    @Override
    public void cambiarContrasena(int idUsuario, String actual, String nueva) throws BLException {
        Usuario usuario = buscarPorId(idUsuario);
        if (usuario == null) {
            throw new NoEncontradoException("No existe la cuenta");
        }
        if (actual == null || !PasswordUtil.verificar(actual, usuario.getContrasena())) {
            throw new ValidacionException("La contraseña actual es incorrecta");
        }
        guardarNuevaContrasena(usuario, nueva);
    }

    @Override
    public void recuperarContrasena(String correo, String dni, String nueva) throws BLException {
        if (correo == null || correo.isBlank() || dni == null || dni.isBlank()) {
            throw new ValidacionException("Ingrese correo y DNI");
        }
        Usuario usuario = buscarPorCorreo(correo.trim());

        // Mismo mensaje en todos los casos: no se revela si el correo existe
        if (usuario == null || !usuario.estaActivo() || !usuario.getDni().equals(dni.trim())) {
            throw new ValidacionException("El correo y el DNI no coinciden con una cuenta activa");
        }
        guardarNuevaContrasena(usuario, nueva);
    }

    //Valida la nueva contraseña y guarda la nueva
    private void guardarNuevaContrasena(Usuario usuario, String nueva) throws BLException {
        if (nueva == null || nueva.length() < 6) {
            throw new ValidacionException("La nueva contraseña debe tener al menos 6 caracteres");
        }
        usuario.setContrasena(PasswordUtil.hashear(nueva));
        if (usuario instanceof Administrador admin) {
            administradorBL.update(admin);
        } else {
            clienteBL.update((Cliente) usuario);
        }
    }

    //Primero busca entre administradores; si no está, entre clientes.
    private Usuario buscarPorId(int id) throws BLException {
        Usuario usuario = administradorBL.findById(id);
        if (usuario == null) {
            usuario = clienteBL.findById(id);
        }
        return usuario;
    }

    //primero busca entre administradores; si no está, entre clientes.
    private Usuario buscarPorCorreo(String correo) throws BLException {
        Usuario usuario = administradorBL.findByCorreo(correo);
        if (usuario == null) {
            usuario = clienteBL.findByCorreo(correo);
        }
        return usuario;
    }
}