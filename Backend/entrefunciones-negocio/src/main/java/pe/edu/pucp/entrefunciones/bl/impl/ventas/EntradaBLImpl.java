package pe.edu.pucp.entrefunciones.bl.impl.ventas;
import java.sql.SQLException;
import java.util.List;
import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.ventas.EntradaBL;
import pe.edu.pucp.entrefunciones.dao.ventas.EntradaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO;
import pe.edu.pucp.entrefunciones.bl.Transacciones;
import pe.edu.pucp.entrefunciones.modelo.ventas.Entrada;

public class EntradaBLImpl implements EntradaBL {

    private final EntradaDAO entradaDAO;
    private final ControlOperacionesDAO control = new ControlOperacionesDAO();

    public EntradaBLImpl(EntradaDAO entradaDAO) {
        if (entradaDAO == null) {
            throw new IllegalArgumentException("El entradaDAO no puede ser nulo");
        }
        this.entradaDAO = entradaDAO;
    }

    @Override
    public List<Entrada> findAll() throws BLException {
        try {
            return entradaDAO.findAll();
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al listar las entradas", ex);
        }
    }

    @Override
    public Entrada findById(Integer id) throws BLException {
        try {
            return entradaDAO.findById(id);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al buscar la entrada " + id, ex);
        }
    }

    @Override
    public void insert(Entrada entrada) throws BLException {
        try {
            entradaDAO.insert(entrada);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al registrar la entrada", ex);
        }
    }

    @Override
    public void update(Entrada entrada) throws BLException {
        try {
            entradaDAO.update(entrada);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al actualizar la entrada", ex);
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        try {
            entradaDAO.delete(id);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al cancelar la entrada " + id, ex);
        }
    }

    @Override
    public List<Entrada> buscarPorReserva(int reservaId) throws BLException {
        try {
            return entradaDAO.findByReserva(reservaId);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al buscar las entradas de la reserva " + reservaId, ex);
        }
    }

    @Override
    public Entrada buscarPorCodigoQR(String codigoQR) throws BLException {
        try {
            return entradaDAO.findByCodigoQR(codigoQR);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al buscar la entrada " + codigoQR, ex);
        }
    }

    @Override
    public void validarIngreso(String codigoQR) throws BLException {
        try {
            Entrada entrada = entradaDAO.findByCodigoQR(codigoQR);
            if (entrada == null) {
                throw new BLException("No existe ninguna entrada con el codigo QR " + codigoQR);
            }
            if (!entrada.validarIngreso()) {
                throw new BLException(
                        "La entrada " + codigoQR + " no se pudo validar: ya fue utilizada o esta cancelada");
            }
            entradaDAO.update(entrada);
        } catch (SQLException ex) {
            throw new BLException("Error de base de datos al validar el ingreso de " + codigoQR, ex);
        }
    }

    @Override
    public void cancelar(int entradaId) throws BLException {
        Transacciones.ejecutar("Error de base de datos al cancelar la entrada " + entradaId, () -> {
            int funcionId = control.funcionDeEntrada(entradaId);
            control.bloquearFuncion(funcionId);
            control.bloquearEntrada(entradaId);
            Entrada entrada = entradaDAO.findById(entradaId);
            if (entrada == null) {
                throw new BLException("No existe la entrada " + entradaId);
            }
            if (!entrada.cancelar()) {
                throw new BLException(
                        "La entrada " + entradaId + " no se pudo cancelar: ya estaba utilizada o cancelada");
            }
            entradaDAO.update(entrada);
            return null;
        });
    }
}
