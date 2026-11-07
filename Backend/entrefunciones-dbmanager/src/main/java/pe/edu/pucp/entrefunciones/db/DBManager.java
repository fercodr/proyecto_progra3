package pe.edu.pucp.entrefunciones.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class DBManager {
    protected String servidor;
    protected int puerto;
    protected String esquema;
    protected String usuario;
    protected String password;

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

    public Connection getConnection() throws SQLException {
        if (usuario == null || password == null) throw new SQLException("Configure DB_USER y DB_PASSWORD");
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new SQLException("No se encontró el driver MySQL", e); }
        return DriverManager.getConnection(
                "jdbc:mysql://" + servidor + ":" + puerto + "/" + esquema + "?connectionTimeZone=-05:00&forceConnectionTimeZoneToSession=true&useInformationSchema=true",
                usuario,
                password
        );
    }
}
