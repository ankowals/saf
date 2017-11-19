package libs.libCore.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    private SharedContext ctx;
    private Storage Storage;
    private FileCore FileCore;

    // PicoContainer injects class SharedContext
    public DBConnector (SharedContext ctx) {
        this.ctx = ctx;
        this.Storage = ctx.Object.get("Storage",Storage.class);
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
    }

    public Connection create() {
        Connection connection = null;
        String JDBC_CONNECTION_URL = Storage.get("Environment.Active.Jdbc.url");
        if ( JDBC_CONNECTION_URL.contains("jdbc:oracle") ) {
            try {
                Log.debug("Try to load oracle driver");
                String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Oracle.path");
                URL u = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
                String classname = "oracle.jdbc.driver.OracleDriver";
                URLClassLoader ucl = new URLClassLoader(new URL[]{u});
                Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
                DriverManager.registerDriver(new DriverShim(d));
                connection = DriverManager.getConnection(JDBC_CONNECTION_URL);
                Log.debug("Connection to " + JDBC_CONNECTION_URL + " is open");
            } catch (ClassNotFoundException e) {
                Log.error("", e);
            } catch (SQLException e) {
                Log.error("", e);
            } catch (IllegalAccessException e) {
                Log.error("", e);
            } catch (InstantiationException e) {
                Log.error("", e);
            } catch (MalformedURLException e) {
                Log.error("", e);
            }
        } else if ( JDBC_CONNECTION_URL.contains("jdbc:sqlserver") ) {
            try {
                Log.debug("Try to load MsSql driver");
                String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Mssql.path");
                URL u = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
                String classname = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                URLClassLoader ucl = new URLClassLoader(new URL[]{u});
                Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
                DriverManager.registerDriver(new DriverShim(d));
                connection = DriverManager.getConnection(JDBC_CONNECTION_URL);
                Log.debug("Connection to " + JDBC_CONNECTION_URL + " is open");

            } catch (ClassNotFoundException e) {
                Log.error("", e);
            } catch (SQLException e) {
                Log.error("", e);
            } catch (IllegalAccessException e) {
                Log.error("", e);
            } catch (InstantiationException e) {
                Log.error("", e);
            } catch (MalformedURLException e) {
                Log.error("", e);
            }
        } else {
            Log.error( "Can't read driver type or wrong name provided." +
                            "Supported drivers types are: jdbc:oracle" );
        }

        return connection;

    }

}
