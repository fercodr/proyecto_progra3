package pe.edu.pucp.entrefunciones.bl.impl.espacios;

import pe.edu.pucp.entrefunciones.bl.BLException;
import pe.edu.pucp.entrefunciones.bl.espacios.SedeBL;
import pe.edu.pucp.entrefunciones.bl.impl.TransaccionBL;
import pe.edu.pucp.entrefunciones.dao.espacios.ButacaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.ButacaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SedeBLImpl implements SedeBL {
    private final SedeDAO sedeDAO;
    private final SalaDAO salaDAO;
    private final ButacaDAO butacaDAO;

    public SedeBLImpl() {
        this(new SedeDAOImpl(), new SalaDAOImpl(), new ButacaDAOImpl());
    }

    /** Conserva el constructor usado por clientes que solo consultan o prueban Sede. */
    public SedeBLImpl(SedeDAO sedeDAO) {
        this(sedeDAO, new SalaDAOImpl(), new ButacaDAOImpl());
    }

    public SedeBLImpl(SedeDAO sedeDAO, SalaDAO salaDAO, ButacaDAO butacaDAO) {
        if (sedeDAO == null || salaDAO == null || butacaDAO == null) {
            throw new IllegalArgumentException("Los DAO del agregado sede no pueden ser nulos");
        }
        this.sedeDAO = sedeDAO;
        this.salaDAO = salaDAO;
        this.butacaDAO = butacaDAO;
    }

    @Override
    public List<Sede> findAll() throws BLException {
        try {
            return sedeDAO.findAll();
        } catch (SQLException e) {
            throw new BLException("No se pudieron listar las sedes", e);
        }
    }

    @Override
    public Sede findById(Integer id) throws BLException {
        validarId(id, "sede");
        try {
            return sedeDAO.findById(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la sede", e);
        }
    }

    @Override
    public Sede findByIdCompleto(Integer id) throws BLException {
        validarId(id, "sede");
        try {
            return sedeDAO.findByIdCompleto(id);
        } catch (SQLException e) {
            throw new BLException("No se pudo recuperar la sede completa", e);
        }
    }

    @Override
    public void insert(Sede sede) throws BLException {
        validarAgregado(sede, true);
        Map<Object, Integer> idsOriginales = capturarIds(sede);
        try {
            TransaccionBL.ejecutar("No se pudo registrar la sede completa", () -> {
                sedeDAO.insert(sede);
                persistirSalasSuministradas(sede, List.of());
            });
        } catch (BLException e) {
            restaurarIds(idsOriginales);
            throw e;
        }
    }

    @Override
    public void update(Sede sede) throws BLException {
        validarAgregado(sede, false);
        validarId(sede.getId(), "sede");
        Map<Object, Integer> idsOriginales = capturarIds(sede);
        try {
            TransaccionBL.ejecutar("No se pudo actualizar la sede completa", () -> {
                validarExiste(sede.getId());
                List<Sala> persistidas = salaDAO.findBySedeId(sede.getId());
                sedeDAO.update(sede);
                persistirSalasSuministradas(sede, persistidas);
            });
        } catch (BLException e) {
            restaurarIds(idsOriginales);
            throw e;
        }
    }

    @Override
    public void sincronizarSalas(Sede sede) throws BLException {
        validarAgregado(sede, false);
        validarId(sede.getId(), "sede");
        Map<Object, Integer> idsOriginales = capturarIds(sede);
        try {
            TransaccionBL.ejecutar("No se pudo sincronizar la sede y sus salas", () -> {
                validarExiste(sede.getId());
                List<Sala> persistidas = salaDAO.findBySedeId(sede.getId());
                sedeDAO.update(sede);
                persistirSalasSuministradas(sede, persistidas);

                Set<Integer> idsConservados = new HashSet<>();
                for (Sala sala : sede.getSalas()) {
                    idsConservados.add(sala.getId());
                }
                for (Sala persistida : persistidas) {
                    if (persistida.estaActivo() && !idsConservados.contains(persistida.getId())) {
                        salaDAO.delete(persistida.getId());
                    }
                }
            });
        } catch (BLException e) {
            restaurarIds(idsOriginales);
            throw e;
        }
    }

    @Override
    public void delete(Integer id) throws BLException {
        validarId(id, "sede");
        TransaccionBL.ejecutar("No se pudo desactivar la sede", () -> {
            Sede existente = validarExiste(id);
            if (existente.estaActivo()) {
                sedeDAO.delete(id);
            }
        });
    }

    private void persistirSalasSuministradas(Sede sede, List<Sala> persistidas)
            throws SQLException, BLException {
        for (Sala sala : sede.getSalas()) {
            sala.setSede(sede);
            Sala persistida = sala.getId() == 0
                    ? buscarSalaPorNombre(persistidas, sala.getNombre(), null)
                    : buscarSalaPorId(persistidas, sala.getId());

            if (sala.getId() > 0 && persistida == null) {
                throw new BLException("La sala " + sala.getId() + " no pertenece a la sede");
            }
            if (sala.getId() == 0 && persistida != null) {
                if (persistida.estaActivo()) {
                    throw new BLException("Ya existe una sala con ese nombre en la sede");
                }
                sala.setId(persistida.getId());
            }
            validarNombreSalaDisponible(persistidas, sala);

            if (persistida == null) {
                salaDAO.insert(sala);
            } else {
                salaDAO.update(sala);
            }
            persistirButacasSuministradas(sala);
            validarAforoPersistido(sala);
        }
    }

    private void persistirButacasSuministradas(Sala sala) throws SQLException, BLException {
        List<Butaca> persistidas = butacaDAO.findBySalaId(sala.getId());
        for (Butaca butaca : sala.getButacas()) {
            butaca.setSala(sala);
            Butaca persistida = butaca.getId() == 0
                    ? buscarButacaPorPosicion(persistidas, butaca, null)
                    : buscarButacaPorId(persistidas, butaca.getId());

            if (butaca.getId() > 0 && persistida == null) {
                throw new BLException("La butaca " + butaca.getId() + " no pertenece a la sala");
            }
            if (butaca.getId() == 0 && persistida != null) {
                if (persistida.estaActivo()) {
                    throw new BLException("Ya existe una butaca en esa fila y columna");
                }
                butaca.setId(persistida.getId());
            }
            if (buscarButacaPorPosicion(persistidas, butaca, butaca.getId()) != null) {
                throw new BLException("Ya existe otra butaca en esa fila y columna");
            }

            if (persistida == null) {
                butacaDAO.insert(butaca);
            } else {
                butacaDAO.update(butaca);
            }
            persistidas = butacaDAO.findBySalaId(sala.getId());
        }
    }

    private void validarAforoPersistido(Sala sala) throws SQLException, BLException {
        long activas = butacaDAO.findBySalaId(sala.getId()).stream()
                .filter(Butaca::estaActivo)
                .count();
        if (activas > sala.getAforo()) {
            throw new BLException("Las butacas activas superan el aforo de la sala "
                    + sala.getNombre());
        }
    }

    private void validarNombreSalaDisponible(List<Sala> persistidas, Sala sala)
            throws BLException {
        Sala duplicada = buscarSalaPorNombre(persistidas, sala.getNombre(), sala.getId());
        if (duplicada != null) {
            throw new BLException("Ya existe otra sala con ese nombre en la sede");
        }
    }

    private Sala buscarSalaPorId(List<Sala> salas, int id) {
        return salas.stream().filter(sala -> sala.getId() == id).findFirst().orElse(null);
    }

    private Sala buscarSalaPorNombre(List<Sala> salas, String nombre, Integer idExcluir) {
        String normalizado = normalizar(nombre);
        return salas.stream()
                .filter(sala -> idExcluir == null || sala.getId() != idExcluir)
                .filter(sala -> normalizar(sala.getNombre()).equals(normalizado))
                .findFirst()
                .orElse(null);
    }

    private Butaca buscarButacaPorId(List<Butaca> butacas, int id) {
        return butacas.stream().filter(butaca -> butaca.getId() == id).findFirst().orElse(null);
    }

    private Butaca buscarButacaPorPosicion(List<Butaca> butacas, Butaca buscada,
                                            Integer idExcluir) {
        return butacas.stream()
                .filter(butaca -> idExcluir == null || butaca.getId() != idExcluir)
                .filter(butaca -> normalizar(butaca.getFila()).equals(normalizar(buscada.getFila())))
                .filter(butaca -> butaca.getColumna() == buscada.getColumna())
                .findFirst()
                .orElse(null);
    }

    private Sede validarExiste(Integer id) throws BLException {
        try {
            Sede sede = sedeDAO.findById(id);
            if (sede == null) {
                throw new BLException("No existe una sede con id " + id);
            }
            return sede;
        } catch (SQLException e) {
            throw new BLException("No se pudo verificar la existencia de la sede", e);
        }
    }

    private void validarAgregado(Sede sede, boolean insercion) throws BLException {
        validarDatosSede(sede);
        if (insercion && sede.getId() != 0) {
            throw new BLException("Una sede nueva no debe tener id asignado");
        }

        Set<String> nombres = new HashSet<>();
        for (Sala sala : sede.getSalas()) {
            if (sala == null) {
                throw new BLException("La colección de salas no puede contener valores nulos");
            }
            if (insercion && sala.getId() != 0) {
                throw new BLException("Una sala nueva no debe tener id asignado");
            }
            validarTexto(sala.getNombre(), 100, "El nombre de la sala");
            validarTexto(sala.getTipoSala(), 50, "El tipo de sala");
            if (sala.getAforo() <= 0) {
                throw new BLException("El aforo debe ser mayor que cero");
            }
            if (!nombres.add(normalizar(sala.getNombre()))) {
                throw new BLException("La sede contiene nombres de sala repetidos");
            }

            Set<String> posiciones = new HashSet<>();
            long activas = 0;
            for (Butaca butaca : sala.getButacas()) {
                if (butaca == null) {
                    throw new BLException("La colección de butacas no puede contener valores nulos");
                }
                if (insercion && butaca.getId() != 0) {
                    throw new BLException("Una butaca nueva no debe tener id asignado");
                }
                validarTexto(butaca.getFila(), 5, "La fila de la butaca");
                if (butaca.getColumna() < 0) {
                    throw new BLException("La columna de la butaca no puede ser negativa");
                }
                validarTexto(butaca.getTipoButaca(), 30, "El tipo de butaca");
                String posicion = normalizar(butaca.getFila()) + "#" + butaca.getColumna();
                if (!posiciones.add(posicion)) {
                    throw new BLException("La sala contiene posiciones de butaca repetidas");
                }
                if (butaca.estaActivo()) {
                    activas++;
                }
            }
            if (activas > sala.getAforo()) {
                throw new BLException("Las butacas activas superan el aforo de la sala "
                        + sala.getNombre());
            }
        }
    }

    private void validarDatosSede(Sede sede) throws BLException {
        if (sede == null) {
            throw new BLException("La sede no puede ser nula");
        }
        validarTexto(sede.getRazonSocial(), 150, "La razón social");
        validarTexto(sede.getDireccion(), 200, "La dirección");
        validarTexto(sede.getCiudad(), 100, "La ciudad");
        if (sede.getTelefonoContacto() != null && sede.getTelefonoContacto().length() > 20) {
            throw new BLException("El teléfono de contacto no puede exceder 20 caracteres");
        }
        if (sede.getSalas() == null) {
            throw new BLException("La colección de salas no puede ser nula");
        }
    }

    private Map<Object, Integer> capturarIds(Sede sede) {
        Map<Object, Integer> ids = new IdentityHashMap<>();
        ids.put(sede, sede.getId());
        for (Sala sala : sede.getSalas()) {
            ids.put(sala, sala.getId());
            for (Butaca butaca : sala.getButacas()) {
                ids.put(butaca, butaca.getId());
            }
        }
        return ids;
    }

    private void restaurarIds(Map<Object, Integer> ids) {
        ids.forEach((entidad, id) -> {
            if (entidad instanceof Sede sede) {
                sede.setId(id);
            } else if (entidad instanceof Sala sala) {
                sala.setId(id);
            } else if (entidad instanceof Butaca butaca) {
                butaca.setId(id);
            }
        });
    }

    private String normalizar(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    private void validarTexto(String valor, int longitudMaxima, String campo) throws BLException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BLException(campo + " no puede estar vacío");
        }
        if (valor.length() > longitudMaxima) {
            throw new BLException(campo + " no puede exceder " + longitudMaxima + " caracteres");
        }
    }

    private void validarId(Integer id, String entidad) throws BLException {
        if (id == null || id <= 0) {
            throw new BLException("El id de la " + entidad + " debe ser positivo");
        }
    }
}
