package modules.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
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
                String pathToDriver = Environment.readProperty("path_to_oracle_driver");
                URL u = new URL("jar:file:"+pathToDriver+"!/");
                String classname = "oracle.jdbc.driver.OracleDriver";
                URLClassLoader ucl = new URLClassLoader(new URL[] { u });
                Driver d = (Driver)Class.forName(classname, true, ucl).newInstance();
                DriverManager.registerDriver(new DriverShim(d));
                connection = DriverManager.getConnection(JDBC_CONNECTION_URL);
                Log.debug("Connection to " + JDBC_CONNECTION_URL + " is open");
            } catch (ClassNotFoundException e) {
                Log.error( "", e );
            } catch (SQLException e) {
                Log.error( "", e );
            } catch (IllegalAccessException e) {
                Log.error( "", e );
            } catch (InstantiationException e) {
                Log.error( "", e );
            } catch (MalformedURLException e) {
                Log.error( "", e );
            }
        } else {
            Log.error( "Can't read driver type or wrong name provided." +
                            "Supported drivers types are: jdbc:oracle" );
        }

        return connection;

    }

}
