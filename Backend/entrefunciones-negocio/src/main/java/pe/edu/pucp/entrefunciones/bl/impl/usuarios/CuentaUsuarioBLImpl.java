package pe.edu.pucp.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.NoEncontradoException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.usuarios.AdministradorBL;
import pe.edu.pucp.entrefunciones.bl.usuarios.ClienteBL;
import pe.edu.pucp.entrefunciones.bl.usuarios.CuentaUsuarioBL;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Usuario;

//No tiene DAO propio: reutiliza los BL de administrador y cliente,

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

        // Mismo mensaje para "no existe", "contraseña mal" e "inactivo":
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

    /** Primero busca entre administradores; si no está, entre clientes. */
    private Usuario buscarPorCorreo(String correo) throws BLException {
        Usuario usuario = administradorBL.findByCorreo(correo);
        if (usuario == null) {
            usuario = clienteBL.findByCorreo(correo);
        }
        return usuario;
    }
}