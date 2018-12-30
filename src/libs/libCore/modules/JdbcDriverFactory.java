package libs.libCore.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("unchecked")
public class JdbcDriverFactory {

    private Context scenarioCtx;
    private Storage Storage;
    private FileCore FileCore;
    private StepCore StepCore;

    public JdbcDriverFactory() {
        this.scenarioCtx = GlobalCtxSingleton.getInstance().get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.Storage = scenarioCtx.get("Storage",Storage.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.StepCore = scenarioCtx.get("StepCore", StepCore.class);
    }


    /**
     * Creates connection to a data base using jdbc driver
     * Connection will be open towards uri defined in the Environment.Active.Jdbc.url
     * Used driver is chosen based on the path defined in Environment.Active.JdbcDrivers.TYPE.path
     * Type can be one of Oracle, Mssql
     *
     * @return Connection
     */
    public Connection create(String connectionString) {

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding ){
            String passwd = Storage.get("Environment.Active.Jdbc.password");
            if ( passwd == null || passwd.equals("") ) {
                Log.error("Environment.Active.UseEncoding=true thus password decoding required but provided password is null or empty!");
            }
            connectionString = connectionString.replace(passwd, StepCore.decodeString(passwd));
        }

        if ( connectionString.contains("jdbc:oracle") ) {
            return createOracleConnection(connectionString);
        }
        if ( connectionString.contains("jdbc:sqlserver") || connectionString.contains("jdbc:jtds:sqlserver") ) {
            return createMsSqlConnection(connectionString);
        } else {
            Log.error("Can't read driver type or wrong name provided. Supported drivers types are: " +
                    "jdbc:oracle, jdbc:sqlserver, jdbc:jtds:sqlserver");
        }

        return null;
    }

    private Connection getConnection(String classname, URL url,String connectionString){
        try {
            URLClassLoader ucl = new URLClassLoader(new URL[]{url});
            Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
            DriverManager.registerDriver(new DriverShim(d));
            Log.debug("Connection to " + connectionString + " is open");

            return DriverManager.getConnection(connectionString);

        } catch (ClassNotFoundException | SQLException | IllegalAccessException | InstantiationException e){
            Log.error(e.getMessage());
        }

        return null;
    }

    private Connection createOracleConnection(String connectionString){
        try {
            Log.debug("Try to load oracle driver");
            String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Oracle.path");
            URL url = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
            String classname = "oracle.jdbc.driver.OracleDriver";

            return getConnection(classname, url, connectionString);

        } catch (MalformedURLException e) {
            Log.error(e.getMessage());
        }

        return null;
    }

    private Connection createMsSqlConnection(String connectionString){
        try {
            Log.debug("Try to load MsSql driver");
            String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Mssql.path");
            URL url = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
            String classname = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

            if ( connectionString.contains("jdbc:jtds:sqlserver") ){
                classname = "net.sourceforge.jtds.jdbc.Driver";
            }

            return getConnection(classname, url, connectionString);

        } catch (MalformedURLException e) {
            Log.error(e.getMessage());
        }

        return null;
    }

}