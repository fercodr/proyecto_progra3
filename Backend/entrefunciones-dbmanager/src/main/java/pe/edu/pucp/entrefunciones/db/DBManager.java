package pe.edu.pucp.entrefunciones.db;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class DBManager {
    protected String servidor;
    protected int puerto;
    protected String esquema;
    protected String usuario;
    protected String password;

    private HikariDataSource dataSource;

    private static DBManager instance;

    protected DBManager() {
    }

    protected DBManager(String servidor,
                        int puerto,
                        String esquema,
                        String usuario,
                        String password) {

        this.servidor = servidor;
        this.puerto = puerto;
        this.esquema = esquema;
        this.usuario = usuario;
        this.password = password;
    }

    public synchronized static DBManager getInstance() {
        if (instance == null) {
            ResourceBundle rb = null;
            try { rb = ResourceBundle.getBundle("db"); } catch (java.util.MissingResourceException ignored) { }
            instance = new DBManager(
                valor("DB_HOST", rb, "servidor", "localhost"),
                Integer.parseInt(valor("DB_PORT", rb, "puerto", "3306")),
                valor("DB_NAME", rb, "esquema", "entrefunciones"),
                valor("DB_USER", rb, "usuario", null),
                valor("DB_PASSWORD", rb, "password", null));
        }
        return instance;
    }

    private static String valor(String env, ResourceBundle rb, String key, String fallback) {
        String value = System.getProperty(env, System.getenv(env));
        if (value == null && rb != null && rb.containsKey(key)) value = rb.getString(key);
        return value == null ? fallback : value;
    }

    // Pool de conexiones (HikariCP). Antes cada getConnection() abria una conexion
    // TCP/TLS nueva contra la BD (local o la RDS compartida) y la cerraba al terminar;
    // con la RDS compartida eso sumaba latencia de red en cada query y, con varios
    // companeros conectados a la vez, podia acercarse al limite de conexiones
    // simultaneas de la instancia. El pool mantiene un numero chico de conexiones ya
    // abiertas y las reutiliza; ConnectionScope.close() sigue llamando a
    // Connection.close(), que con Hikari no cierra la conexion real sino que la
    // devuelve al pool, asi que no hizo falta tocar nada en la capa de DAOs.
    private synchronized HikariDataSource dataSource() {
        if (dataSource == null) {
            if (usuario == null || password == null) {
                throw new IllegalStateException("Configure DB_USER y DB_PASSWORD");
            }
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + servidor + ":" + puerto + "/" + esquema
                    + "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(usuario);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            // Tamano deliberadamente chico: la RDS compartida la usan varios companeros
            // a la vez, asi que cada instancia del backend debe pedir pocas conexiones.
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(300_000);
            config.setMaxLifetime(600_000);
            config.setPoolName("entrefunciones-pool");
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        try {
            return dataSource().getConnection();
        } catch (IllegalStateException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }
}
