package pe.edu.pucp.entrefunciones.dao.impl.espacios;

import pe.edu.pucp.entrefunciones.dao.espacios.SedeDAO;
import pe.edu.pucp.entrefunciones.dao.transacciones.ConnectionScope;
import pe.edu.pucp.entrefunciones.dao.transacciones.TransactionsManager;
import pe.edu.pucp.entrefunciones.modelo.espacios.Butaca;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sala;
import pe.edu.pucp.entrefunciones.modelo.espacios.Sede;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SedeDAOImpl implements SedeDAO {

    @Override
    public List<Sede> findAll() throws SQLException {
        try (ConnectionScope scope = ConnectionScope.openForRead()) {
            String sql = "{call listar_sedes()}";
            try (CallableStatement cmd = scope.connection().prepareCall(sql);
                 ResultSet rs = cmd.executeQuery()) {
                List<Sede> sedes = new ArrayList<>();
                while (rs.next()) {
                    sedes.add(mapearSede(rs));
                }
                return sedes;
            }
        }
    }

    @Override
    public Sede findById(Integer id) throws SQLException {
        validarId(id);
        try (ConnectionScope scope = ConnectionScope.openForRead()) {
            return findById(scope.connection(), id);
        }
    }

    @Override
    public Sede findByIdCompleto(Integer id) throws SQLException {
        validarId(id);
        try (ConnectionScope scope = ConnectionScope.openForRead()) {
            Connection connection = scope.connection();
            Sede sede = findById(connection, id);
            if (sede == null) {
                return null;
            }

            Map<Integer, Sala> salas = cargarSalas(connection, sede);
            cargarButacas(connection, sede.getId(), salas);
            return sede;
        }
    }

    @Override
    public List<Sede> findByIds(Collection<Integer> ids) throws SQLException {
        List<Integer> validos = ids == null ? List.of() : ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (validos.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(validos.size(), "?"));
        String sql = "SELECT id, razon_social, direccion, ciudad, distrito, telefono_contacto, activo "
                + "FROM sede WHERE id IN (" + placeholders + ") ORDER BY id";
        try (ConnectionScope scope = ConnectionScope.openForRead();
             PreparedStatement cmd = scope.connection().prepareStatement(sql)) {
            for (int i = 0; i < validos.size(); i++) {
                cmd.setInt(i + 1, validos.get(i));
            }
            try (ResultSet rs = cmd.executeQuery()) {
                List<Sede> sedes = new ArrayList<>();
                while (rs.next()) {
                    sedes.add(mapearSede(rs));
                }
                return sedes;
            }
        }
    }

    @Override
    public void insert(Sede sede) throws SQLException {
        validarSede(sede);
        Connection connection = TransactionsManager.getConnection();
        String sql = "{call insertar_sede(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            asignarParametros(cmd, sede);
            cmd.registerOutParameter(7, Types.INTEGER);
            cmd.executeUpdate();
            sede.setId(cmd.getInt(7));
            if (sede.getId() <= 0) {
                throw new SQLException("No se obtuvo el id de la sede insertada");
            }
        }
    }

    @Override
    public void update(Sede sede) throws SQLException {
        validarSede(sede);
        validarId(sede.getId());
        Connection connection = TransactionsManager.getConnection();
        String sql = "{call modificar_sede(?, ?, ?, ?, ?, ?, ?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt(1, sede.getId());
            cmd.setString(2, sede.getRazonSocial());
            cmd.setString(3, sede.getDireccion());
            cmd.setString(4, sede.getCiudad());
            cmd.setString(5, sede.getDistrito());
            cmd.setString(6, sede.getTelefonoContacto());
            cmd.setBoolean(7, sede.estaActivo());
            cmd.executeUpdate();
            if (!existe(connection, sede.getId())) {
                throw new SQLException("No existe la sede con id " + sede.getId());
            }
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        validarId(id);
        Connection connection = TransactionsManager.getConnection();
        String sql = "{call eliminar_sede(?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt(1, id);
            cmd.executeUpdate();
            if (!existe(connection, id)) {
                throw new SQLException("No existe la sede con id " + id);
            }
        }
    }

    private Sede findById(Connection connection, int id) throws SQLException {
        String sql = "{call buscar_sede_por_id(?)}";
        try (CallableStatement cmd = connection.prepareCall(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next() ? mapearSede(rs) : null;
            }
        }
    }

    private Map<Integer, Sala> cargarSalas(Connection connection, Sede sede) throws SQLException {
        String sql = """
                SELECT id, nombre, aforo, tipo_sala, accesible, activo
                FROM sala
                WHERE sede_id = ?
                ORDER BY id
                """;
        Map<Integer, Sala> salas = new LinkedHashMap<>();
        try (PreparedStatement cmd = connection.prepareStatement(sql)) {
            cmd.setInt(1, sede.getId());
            try (ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    Sala sala = new Sala(
                            rs.getInt("id"), sede, rs.getString("nombre"),
                            rs.getInt("aforo"), rs.getString("tipo_sala"));
                    sala.setAccesible(rs.getBoolean("accesible"));
                    if (rs.getBoolean("activo")) {
                        sala.activar();
                    } else {
                        sala.desactivar();
                    }
                    sede.agregarSala(sala);
                    salas.put(sala.getId(), sala);
                }
            }
        }
        return salas;
    }

    private void cargarButacas(Connection connection, int sedeId, Map<Integer, Sala> salas)
            throws SQLException {
        if (salas.isEmpty()) {
            return;
        }
        String sql = """
                SELECT b.id, b.sala_id, b.fila, b.columna, b.tipo_butaca, b.activo
                FROM butaca b
                INNER JOIN sala sa ON sa.id = b.sala_id
                WHERE sa.sede_id = ?
                ORDER BY b.sala_id, b.id
                """;
        try (PreparedStatement cmd = connection.prepareStatement(sql)) {
            cmd.setInt(1, sedeId);
            try (ResultSet rs = cmd.executeQuery()) {
                while (rs.next()) {
                    Sala sala = salas.get(rs.getInt("sala_id"));
                    Butaca butaca = new Butaca(
                            rs.getInt("id"), sala, rs.getString("fila"),
                            rs.getInt("columna"), rs.getString("tipo_butaca"));
                    if (rs.getBoolean("activo")) {
                        butaca.activar();
                    } else {
                        butaca.desactivar();
                    }
                    sala.agregarButaca(butaca);
                }
            }
        }
    }

    private boolean existe(Connection connection, int id) throws SQLException {
        String sql = "SELECT 1 FROM sede WHERE id = ?";
        try (PreparedStatement cmd = connection.prepareStatement(sql)) {
            cmd.setInt(1, id);
            try (ResultSet rs = cmd.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void asignarParametros(CallableStatement cmd, Sede sede) throws SQLException {
        cmd.setString(1, sede.getRazonSocial());
        cmd.setString(2, sede.getDireccion());
        cmd.setString(3, sede.getCiudad());
        cmd.setString(4, sede.getDistrito());
        cmd.setString(5, sede.getTelefonoContacto());
        cmd.setBoolean(6, sede.estaActivo());
    }

    private Sede mapearSede(ResultSet rs) throws SQLException {
        Sede sede = new Sede();
        sede.setId(rs.getInt("id"));
        sede.setRazonSocial(rs.getString("razon_social"));
        sede.setDireccion(rs.getString("direccion"));
        sede.setCiudad(rs.getString("ciudad"));
        sede.setDistrito(rs.getString("distrito"));
        sede.setTelefonoContacto(rs.getString("telefono_contacto"));
        if (rs.getBoolean("activo")) {
            sede.activar();
        } else {
            sede.desactivar();
        }
        return sede;
    }

    private void validarSede(Sede sede) {
        if (sede == null) {
            throw new IllegalArgumentException("La sede no puede ser nula");
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id de la sede debe ser positivo");
        }
    }
}
