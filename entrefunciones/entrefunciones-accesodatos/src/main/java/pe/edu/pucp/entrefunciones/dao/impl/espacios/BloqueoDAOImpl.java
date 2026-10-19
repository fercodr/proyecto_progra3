package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.BloqueoDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Bloqueo;
import pe.edu.pucp.entrefunciones.modelo.espacios.Recurso;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BloqueoDAOImpl implements BloqueoDAO {

    @Override
    public List<Bloqueo> findAll() throws SQLException {
        String sql = """
                SELECT bl.id, bl.motivo, bl.fecha_inicio, bl.fecha_fin,
                       bl.recurso_id AS recurso_id, r.nombre AS recurso_nombre,
                       r.tipo AS recurso_tipo, r.activo AS recurso_activo,
                       bl.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM bloqueo bl
                LEFT JOIN recurso r ON r.id = bl.recurso_id
                LEFT JOIN sala sa ON sa.id = bl.sala_id
                LEFT JOIN sede se ON se.id = sa.sede_id
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql);
             ResultSet rs = cmd.executeQuery()) {
            return mapearLista(rs);
        }
    }

    @Override
    public List<Bloqueo> findBySalaId(int salaId) throws SQLException {
        String sql = """
                SELECT bl.id, bl.motivo, bl.fecha_inicio, bl.fecha_fin,
                       bl.recurso_id AS recurso_id, r.nombre AS recurso_nombre,
                       r.tipo AS recurso_tipo, r.activo AS recurso_activo,
                       bl.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM bloqueo bl
                LEFT JOIN recurso r ON r.id = bl.recurso_id
                LEFT JOIN sala sa ON sa.id = bl.sala_id
                LEFT JOIN sede se ON se.id = sa.sede_id
                WHERE bl.sala_id = ?
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, salaId);
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearLista(rs);
            }
        }
    }

    @Override
    public List<Bloqueo> findByRecursoId(int recursoId) throws SQLException {
        String sql = """
                SELECT bl.id, bl.motivo, bl.fecha_inicio, bl.fecha_fin,
                       bl.recurso_id AS recurso_id, r.nombre AS recurso_nombre,
                       r.tipo AS recurso_tipo, r.activo AS recurso_activo,
                       bl.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM bloqueo bl
                LEFT JOIN recurso r ON r.id = bl.recurso_id
                LEFT JOIN sala sa ON sa.id = bl.sala_id
                LEFT JOIN sede se ON se.id = sa.sede_id
                WHERE bl.recurso_id = ?
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, recursoId);
            try (ResultSet rs = cmd.executeQuery()) {
                return mapearLista(rs);
            }
        }
    }

    @Override
    public boolean existsOverlapBySalaId(int salaId, LocalDateTime fechaInicio,
                                         LocalDateTime fechaFin, Integer idExcluir)
            throws SQLException {
        return existsOverlap("sala_id", salaId, fechaInicio, fechaFin, idExcluir);
    }

    @Override
    public boolean existsOverlapByRecursoId(int recursoId, LocalDateTime fechaInicio,
                                            LocalDateTime fechaFin, Integer idExcluir)
            throws SQLException {
        return existsOverlap("recurso_id", recursoId, fechaInicio, fechaFin, idExcluir);
    }

    @Override
    public Bloqueo findById(Integer id) throws SQLException {
        validarId(id);
        String sql = """
                SELECT bl.id, bl.motivo, bl.fecha_inicio, bl.fecha_fin,
                       bl.recurso_id AS recurso_id, r.nombre AS recurso_nombre,
                       r.tipo AS recurso_tipo, r.activo AS recurso_activo,
                       bl.sala_id AS sala_id, sa.nombre AS sala_nombre,
                       sa.aforo AS sala_aforo, sa.tipo_sala,
                       sa.activo AS sala_activo,
                       se.id AS sede_id, se.razon_social AS sede_razon_social,
                       se.direccion AS sede_direccion, se.ciudad AS sede_ciudad,
                       se.telefono_contacto AS sede_telefono_contacto,
                       se.activo AS sede_activo
                FROM bloqueo bl
                LEFT JOIN recurso r ON r.id = bl.recurso_id
                LEFT JOIN sala sa ON sa.id = bl.sala_id
                LEFT JOIN sede se ON se.id = sa.sede_id
                WHERE bl.id = ?
                """;
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insert(Bloqueo bloqueo) throws SQLException {
        validarBloqueo(bloqueo);
        String sql = """
                INSERT INTO bloqueo (motivo, fecha_inicio, fecha_fin, sala_id, recurso_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(cmd, bloqueo);
            if (cmd.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el bloqueo");
            }
            asignarIdGenerado(cmd, bloqueo);
        }
    }

    @Override
    public void update(Bloqueo bloqueo) throws SQLException {
        validarBloqueo(bloqueo);
        String sql = """
                UPDATE bloqueo
                SET motivo = ?, fecha_inicio = ?, fecha_fin = ?, sala_id = ?, recurso_id = ?
                WHERE id = ?
                """;
        Connection conn = TransactionsManager.getConnection();
        try (PreparedStatement cmd = conn.prepareStatement(sql)) {
            asignarParametros(cmd, bloqueo);
            cmd.setInt(6, bloqueo.getId());
            if (cmd.executeUpdate() == 0 && !existe(conn, bloqueo.getId())) {
                throw new SQLException("No se pudo actualizar el bloqueo con id " + bloqueo.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        throw new SQLException(
                "El esquema actual no admite baja lógica de bloqueos y se prohíbe el borrado físico");
    }

    private void asignarParametros(PreparedStatement cmd, Bloqueo bloqueo) throws SQLException {
        cmd.setString(1, bloqueo.getMotivo());
        cmd.setTimestamp(2, Timestamp.valueOf(bloqueo.getFechaInicio()));
        cmd.setTimestamp(3, Timestamp.valueOf(bloqueo.getFechaFin()));
        if (bloqueo.getSala() != null) {
            cmd.setInt(4, bloqueo.getSala().getId());
            cmd.setNull(5, Types.INTEGER);
        } else {
            cmd.setNull(4, Types.INTEGER);
            cmd.setInt(5, bloqueo.getRecurso().getId());
        }
    }

    private boolean existsOverlap(String columnaObjetivo, int objetivoId,
                                  LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                  Integer idExcluir) throws SQLException {
        String sql = """
                SELECT 1
                FROM bloqueo
                WHERE %s = ?
                  AND fecha_inicio < ?
                  AND fecha_fin > ?
                  AND (? IS NULL OR id <> ?)
                LIMIT 1
                """.formatted(columnaObjetivo);
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            cmd.setInt(1, objetivoId);
            cmd.setTimestamp(2, Timestamp.valueOf(fechaFin));
            cmd.setTimestamp(3, Timestamp.valueOf(fechaInicio));
            if (idExcluir == null) {
                cmd.setNull(4, Types.INTEGER);
                cmd.setNull(5, Types.INTEGER);
            } else {
                cmd.setInt(4, idExcluir);
                cmd.setInt(5, idExcluir);
            }
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existe(Connection conn, int id) throws SQLException {
        try (PreparedStatement cmd = conn.prepareStatement(
                "SELECT 1 FROM bloqueo WHERE id = ?")) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Bloqueo> mapearLista(ResultSet rs) throws SQLException {
        List<Bloqueo> bloqueos = new ArrayList<>();
        while (rs.next()) {
            bloqueos.add(mapear(rs));
        }
        return bloqueos;
    }

    private Bloqueo mapear(ResultSet rs) throws SQLException {
        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setId(rs.getInt("id"));
        bloqueo.setMotivo(rs.getString("motivo"));
        bloqueo.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        bloqueo.setFechaFin(rs.getTimestamp("fecha_fin").toLocalDateTime());

        int salaId = rs.getInt("sala_id");
        if (!rs.wasNull()) {
            bloqueo.setSala(mapearSala(rs, salaId));
        }

        int recursoId = rs.getInt("recurso_id");
        if (!rs.wasNull()) {
            bloqueo.setRecurso(mapearRecurso(rs, recursoId));
        }
        return bloqueo;
    }

    private Sala mapearSala(ResultSet rs, int salaId) throws SQLException {
        Sede sede = new Sede();
        sede.setId(rs.getInt("sede_id"));
        sede.setRazonSocial(rs.getString("sede_razon_social"));
        sede.setDireccion(rs.getString("sede_direccion"));
        sede.setCiudad(rs.getString("sede_ciudad"));
        sede.setTelefonoContacto(rs.getString("sede_telefono_contacto"));
        if (rs.getBoolean("sede_activo")) {
            sede.activar();
        } else {
            sede.desactivar();
        }

        Sala sala = new Sala(
                salaId,
                sede,
                rs.getString("sala_nombre"),
                rs.getInt("sala_aforo"),
                rs.getString("tipo_sala")
        );
        if (rs.getBoolean("sala_activo")) {
            sala.activar();
        } else {
            sala.desactivar();
        }
        return sala;
    }

    private Recurso mapearRecurso(ResultSet rs, int recursoId) throws SQLException {
        Recurso recurso = new Recurso(
                recursoId,
                rs.getString("recurso_nombre"),
                rs.getString("recurso_tipo")
        );
        if (rs.getBoolean("recurso_activo")) {
            recurso.activar();
        } else {
            recurso.desactivar();
        }
        return recurso;
    }

    private void asignarIdGenerado(PreparedStatement cmd, Bloqueo bloqueo) throws SQLException {
        try (ResultSet keys = cmd.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No se obtuvo el id del bloqueo insertado");
            }
            bloqueo.setId(keys.getInt(1));
        }
    }

    private void validarBloqueo(Bloqueo bloqueo) {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo no puede ser nulo");
        }
        if ((bloqueo.getSala() == null) == (bloqueo.getRecurso() == null)) {
            throw new IllegalArgumentException(
                    "El bloqueo debe tener exactamente una sala o un recurso");
        }
        if (bloqueo.getFechaInicio() == null || bloqueo.getFechaFin() == null) {
            throw new IllegalArgumentException("Las fechas del bloqueo no pueden ser nulas");
        }
        if (!bloqueo.getFechaFin().isAfter(bloqueo.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
    }
}
