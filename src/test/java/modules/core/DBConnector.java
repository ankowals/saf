package modules.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    private SharedContext ctx;
    private PropertyReader Environment;
    private String JDBC_CONNECTION_URL;

    // PicoContainer injects class SharedContext
    public DBConnector (SharedContext ctx) {
        this.ctx = ctx;
        this.Environment = ctx.Object.get("Environment",PropertyReader.class);
    }

    public Connection create() {
        Connection connection = null;
        JDBC_CONNECTION_URL = Environment.readProperty("JDBC_CONNECTION_url");
        if ( JDBC_CONNECTION_URL.contains("jdbc:oracle") ) {
            try {
                Log.debug("Try to load oracle driver");
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(JDBC_CONNECTION_URL);
                Log.debug("Connection to " + JDBC_CONNECTION_URL + " is open");
            } catch (ClassNotFoundException e) {
                Log.error( "", e );
            } catch (SQLException e) {
                Log.error( "", e );
            }
        } else {
            Log.error( "Can't read driver type or wrong name provided." +
                            "Supported drivers types are: jdbc:oracle" );
        }

        return connection;

    }

}
