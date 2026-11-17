package pe.edu.pucp.entrefunciones.bl.impl.eventos;

import pe.edu.pucp.entrefunciones.bl.error.BLException;
import pe.edu.pucp.entrefunciones.bl.error.ValidacionException;
import pe.edu.pucp.entrefunciones.bl.eventos.FuncionBL;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.EventoDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAO;
import pe.edu.pucp.entrefunciones.dao.impl.eventos.FuncionDAOImpl;
import pe.edu.pucp.entrefunciones.dao.espacios.SalaDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl;
import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.SedeDAOImpl;
import pe.edu.pucp.entrefunciones.dao.impl.espacios.BloqueoSalaDAOImpl;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Funcion;
import pe.edu.pucp.entrefunciones.modelo.catalogo.Evento;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.SQLException;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FuncionBLImpl implements FuncionBL {

    private static final Set<String> TIPOS_EVENTO_REPORTE = Set.of(
            "PELICULA", "OBRA_TEATRO", "ACTIVIDAD_CULTURAL");

    private final FuncionDAO funcionDAO;
    private final EventoDAO eventoDAO;
    private final SalaDAO salaDAO;
    private final SedeDAO sedeDAO;
    private final pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO control = new pe.edu.pucp.entrefunciones.dao.impl.ControlOperacionesDAO();
    private final BloqueoSalaDAOImpl bloqueoSalaDAO = new BloqueoSalaDAOImpl();

    public FuncionBLImpl() {
        this(new FuncionDAOImpl(), new EventoDAOImpl(),
                new SalaDAOImpl(), new SedeDAOImpl());
    }

    FuncionBLImpl(FuncionDAO funcionDAO, EventoDAO eventoDAO,
                  SalaDAO salaDAO, SedeDAO sedeDAO) {
        this.funcionDAO = Objects.requireNonNull(funcionDAO);
        this.eventoDAO = Objects.requireNonNull(eventoDAO);
        this.salaDAO = Objects.requireNonNull(salaDAO);
        this.sedeDAO = Objects.requireNonNull(sedeDAO);
    }

    private void validarDisponibilidad(Funcion funcion) throws SQLException, BLException {
        control.bloquearSala(funcion.getSala().getId());
        var sala = new pe.edu.pucp.entrefunciones.dao.impl.espacios.SalaDAOImpl().findById(funcion.getSala().getId());
        if(sala==null || !sala.estaActivo() || !sala.getSede().estaActivo()) throw new BLException("La sala o sede no está activa");
        if(funcion.getEstado()==pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CONFIRMADA
                && bloqueoSalaDAO.existeSolapamiento(
                    sala.getId(),
                    funcion.getFecha().atTime(funcion.getHoraInicio()),
                    funcion.getFecha().atTime(funcion.getHoraFin()), null))
            throw new BLException("El horario se cruza con un bloqueo vigente de la sala");
        if(funcion.getEstado()==pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CONFIRMADA)
            for(Funcion otra:funcionDAO.findBySalaId(sala.getId()))
                if(otra.getId()!=funcion.getId() && otra.getEstado()==pe.edu.pucp.entrefunciones.modelo.funciones.EstadoFuncion.CONFIRMADA
                    && otra.getFecha().equals(funcion.getFecha())
                    && funcion.getHoraInicio().isBefore(otra.getHoraFin()) && funcion.getHoraFin().isAfter(otra.getHoraInicio()))
                    throw new BLException("El horario se cruza con otra función de la misma sala");
    }

    @Override
    public List<Funcion> findAll() throws BLException {
        try {
            return funcionDAO.findAll();

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo listar las funciones", e);
        }
    }

    @Override
    public Funcion findById(Integer id) throws BLException {
        try {
            return funcionDAO.findById(id);

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo recuperar la función", e);
        }
    }

    @Override
    public List<Funcion> findReportes(LocalDate desde, LocalDate hasta,
                                      Integer idSede, Integer idSala,
                                      String tipoEvento)
            throws BLException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ValidacionException("La fecha desde no puede ser posterior a hasta");
        }
        if (idSede != null && idSede <= 0) {
            throw new ValidacionException("El identificador de sede debe ser positivo");
        }
        if (idSala != null && idSala <= 0) {
            throw new ValidacionException("El identificador de sala debe ser positivo");
        }
        String tipoEventoNormalizado = normalizarTipoEvento(tipoEvento);

        try {
            Map<Integer, Sede> sedesPorId = new HashMap<>();
            Map<Integer, Sala> salasPorId = new HashMap<>();

            if (idSede != null) {
                Sede sedeFiltro = sedeDAO.findById(idSede);
                if (sedeFiltro == null) {
                    throw new ValidacionException("No existe la sede indicada");
                }
                sedesPorId.put(sedeFiltro.getId(), sedeFiltro);
            }

            if (idSala != null) {
                Sala salaFiltro = salaDAO.findById(idSala);
                if (salaFiltro == null) {
                    throw new ValidacionException("No existe la sala indicada");
                }
                salasPorId.put(salaFiltro.getId(), salaFiltro);
                if (sedeCompleta(salaFiltro.getSede())) {
                    sedesPorId.putIfAbsent(salaFiltro.getSede().getId(), salaFiltro.getSede());
                }
                if (idSede != null && (salaFiltro.getSede() == null
                        || salaFiltro.getSede().getId() != idSede)) {
                    throw new ValidacionException("La sala no pertenece a la sede indicada");
                }
            }

            List<Funcion> resultados = funcionDAO.findReportes(
                    desde, hasta, idSede, idSala, tipoEventoNormalizado);

            Set<Integer> idsSala = new HashSet<>();
            Set<Integer> idsSede = new HashSet<>();
            Set<Integer> idsEvento = new HashSet<>();
            Map<Integer, Evento> eventosPorId = new HashMap<>();
            for (Funcion funcion : resultados) {
                if (funcion.getSala() == null || funcion.getEvento() == null
                        || funcion.getSala().getSede() == null) {
                    throw new BLException("La función del reporte no tiene relaciones obligatorias completas");
                }
                idsSala.add(funcion.getSala().getId());
                idsSede.add(funcion.getSala().getSede().getId());
                idsEvento.add(funcion.getEvento().getId());
                if (salaCompleta(funcion.getSala())) {
                    salasPorId.putIfAbsent(funcion.getSala().getId(), funcion.getSala());
                }
                if (sedeCompleta(funcion.getSala().getSede())) {
                    sedesPorId.putIfAbsent(funcion.getSala().getSede().getId(), funcion.getSala().getSede());
                }
                if (eventoCompleto(funcion.getEvento())) {
                    eventosPorId.putIfAbsent(funcion.getEvento().getId(), funcion.getEvento());
                }
            }

            idsSala.removeAll(salasPorId.keySet());
            if (!idsSala.isEmpty()) {
                for (Sala sala : salaDAO.findByIds(idsSala)) {
                    salasPorId.put(sala.getId(), sala);
                    if (sedeCompleta(sala.getSede())) {
                        sedesPorId.putIfAbsent(sala.getSede().getId(), sala.getSede());
                    }
                }
            }

            idsSede.removeAll(sedesPorId.keySet());
            if (!idsSede.isEmpty()) {
                for (Sede sede : sedeDAO.findByIds(idsSede)) {
                    sedesPorId.put(sede.getId(), sede);
                }
            }

            idsEvento.removeAll(eventosPorId.keySet());
            for (Evento evento : eventoDAO.findByIds(idsEvento)) {
                eventosPorId.put(evento.getId(), evento);
            }

            Set<Integer> salasAsociadas = new HashSet<>();
            for (Funcion funcion : resultados) {
                int idSalaResultado = funcion.getSala().getId();
                int idSedeResultado = funcion.getSala().getSede().getId();
                int idEventoResultado = funcion.getEvento().getId();
                Sala sala = salasPorId.get(idSalaResultado);
                Sede sede = sedesPorId.get(idSedeResultado);
                Evento evento = eventosPorId.get(idEventoResultado);
                if (sala == null || sede == null || evento == null) {
                    throw new BLException("No se pudieron recuperar las relaciones de la función "
                            + funcion.getId());
                }
                sala.setSede(sede);
                funcion.setSala(sala);
                funcion.setEvento(evento);
                evento.agregarFuncion(funcion);
                sala.agregarFuncion(funcion);
                if (salasAsociadas.add(sala.getId())) {
                    sede.agregarSala(sala);
                }

                int aforo = funcion.getSala() == null ? 0 : funcion.getSala().getAforo();
                Integer entradas = funcion.getEntradasVigentes();
                if (aforo <= 0 || entradas == null) {
                    funcion.setPorcentajeOcupacion(null);
                } else {
                    funcion.setPorcentajeOcupacion(
                            BigDecimal.valueOf(entradas)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(aforo), 2, RoundingMode.HALF_UP));
                }
            }
            return resultados;
        } catch (SQLException e) {
            throw new BLException("No se pudo consultar el reporte de funciones", e);
        }
    }

    private String normalizarTipoEvento(String tipoEvento) throws ValidacionException {
        if (tipoEvento == null || tipoEvento.isBlank()) {
            return null;
        }
        String normalizado = tipoEvento.trim();
        if (!TIPOS_EVENTO_REPORTE.contains(normalizado)) {
            throw new ValidacionException("El tipo de actividad no es válido");
        }
        return normalizado;
    }

    private boolean salaCompleta(Sala sala) {
        return sala.getId() > 0
                && sala.getNombre() != null && !sala.getNombre().isBlank()
                && sala.getAforo() > 0
                && sala.getTipoSala() != null && !sala.getTipoSala().isBlank()
                && sedeCompleta(sala.getSede());
    }

    private boolean sedeCompleta(Sede sede) {
        return sede != null && sede.getId() > 0
                && sede.getRazonSocial() != null && !sede.getRazonSocial().isBlank()
                && sede.getDireccion() != null && !sede.getDireccion().isBlank()
                && sede.getCiudad() != null && !sede.getCiudad().isBlank();
    }

    private boolean eventoCompleto(Evento evento) {
        return evento != null && evento.getId() > 0
                && evento.getTitulo() != null && !evento.getTitulo().isBlank();
    }

    @Override public void insert(Funcion funcion) throws BLException {
        validar(funcion);
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo programar la función",()-> {
            validarEvento(funcion);validarDisponibilidad(funcion);funcionDAO.insert(funcion);return null;
        });
    }
    @Override public void update(Funcion funcion) throws BLException {
        validar(funcion);
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo actualizar la función",()-> {
            validarExiste(funcion.getId());validarEvento(funcion);validarDisponibilidad(funcion);
            funcionDAO.update(funcion);return null;
        });
    }
    @Override public void delete(Integer id) throws BLException {
        pe.edu.pucp.entrefunciones.bl.Transacciones.ejecutar("No se pudo cancelar la función",()-> {
            validarExiste(id);funcionDAO.delete(id);return null;
        });
    }

    private void validar(Funcion funcion) throws BLException {

        if (funcion == null) {
            throw new BLException(
                    "La función no puede ser nula");
        }

        if (funcion.getEvento() == null) {
            throw new BLException(
                    "La función debe pertenecer a un evento");
        }

        if (funcion.getSala() == null) {
            throw new BLException(
                    "La función debe tener una sala");
        }

        if (funcion.getFecha() == null) {
            throw new BLException(
                    "La función debe tener una fecha");
        }

        if (funcion.getHoraInicio() == null) {
            throw new BLException(
                    "La función debe tener una hora de inicio");
        }

        if (funcion.getHoraFin() == null) {
            throw new BLException(
                    "La función debe tener una hora de fin");
        }

        if (!funcion.getHoraFin()
                .isAfter(funcion.getHoraInicio())) {
            throw new BLException(
                    "La hora de fin debe ser posterior al inicio en el mismo día; divida los horarios que cruzan medianoche");
        }

        if (funcion.getIdioma() == null
                || funcion.getIdioma().trim().isEmpty()) {
            throw new BLException(
                    "El idioma de la función no puede estar vacío");
        }

        if (funcion.getEstado() == null) {
            throw new BLException(
                    "La función debe tener un estado");
        }
    }

    private void validarEvento(Funcion funcion)
            throws BLException {

        try {
            var evento = eventoDAO.findById(funcion.getEvento().getId());
            if (evento == null || !evento.estaActivo()) {

                throw new BLException(
                        "No existe el evento asociado a la función");
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar el evento de la función", e);
        }
    }

    private void validarExiste(Integer id)
            throws BLException {

        if (id == null) {
            throw new BLException(
                    "El id de la función no puede ser nulo");
        }

        try {
            if (funcionDAO.findById(id) == null) {
                throw new BLException(
                        "No existe una función con id " + id);
            }

        } catch (SQLException e) {
            throw new BLException(
                    "No se pudo verificar la existencia de la función", e);
        }
    }
}
