package pe.edu.pucp.negocio.entrefunciones.bl.impl.usuarios;

import pe.edu.pucp.entrefunciones.dao.impl.usuarios.AdministradorDAOImpl;
import pe.edu.pucp.entrefunciones.dao.usuarios.AdministradorDAO;
import pe.edu.pucp.entrefunciones.modelo.usuarios.Administrador;
import pe.edu.pucp.negocio.entrefunciones.bl.BLException;
import pe.edu.pucp.negocio.entrefunciones.bl.usuarios.AdministradorBL;

import java.sql.SQLException;

public class AdministradorBLImpl extends UsuarioBLImpl<Administrador>
        implements AdministradorBL {

    private final AdministradorDAO administradorDAO;

    public AdministradorBLImpl() {
        this(new AdministradorDAOImpl());
    }

    private AdministradorBLImpl(AdministradorDAO administradorDAO) {
        super(administradorDAO);
        this.administradorDAO = administradorDAO;
    }

    @Override
    public Administrador findByCodigoEmpleado(String codigoEmpleado) throws BLException {
        try {
            return administradorDAO.findByCodigoEmpleado(codigoEmpleado);
        } catch (SQLException e) {
            throw new BLException("No se pudo buscar el administrador por código de empleado", e);
        }
    }

    @Override
    protected void validarFormato(Administrador administrador) throws BLException {
        super.validarFormato(administrador);
        if (administrador.getCodigoEmpleado() == null
                || administrador.getCodigoEmpleado().trim().isEmpty()) {
            throw new BLException("El código de empleado es obligatorio");
        }
    }
}
