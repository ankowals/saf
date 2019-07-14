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
    public Connection create(String dbIdentifier) {
        String url = Storage.get("Environment.Active.Db." + dbIdentifier + ".url"); //user provided connection string
        String type = Storage.get("Environment.Active.Db." + dbIdentifier + ".type");
        String host = Storage.get("Environment.Active.Db." + dbIdentifier + ".host");
        Integer port = Storage.get("Environment.Active.Db." + dbIdentifier + ".port");
        String database = Storage.get("Environment.Active.Db." + dbIdentifier + ".database");
        String instance = Storage.get("Environment.Active.Db." + dbIdentifier + ".instance");
        String user = Storage.get("Environment.Active.Db." + dbIdentifier + ".user");
        String passwd = Storage.get("Environment.Active.Db." + dbIdentifier + ".password");

        //allow to use user connection string
        if ( url == null ) {
            if (type == null) {
                Log.error("Environment.Active.Db." + dbIdentifier + ".type is null or empty!");
            }
            if (host == null) {
                Log.error("Environment.Active.Db." + dbIdentifier + ".host is null or empty!");
            }
            if (port == null) {
                Log.error("Environment.Active.Db." + dbIdentifier + ".port is null or empty!");
            }
            if (user == null) {
                Log.error("Environment.Active.Db." + dbIdentifier + ".user is null or empty!");
            }
            if (passwd == null) {
                Log.error("Environment.Active.Db." + dbIdentifier + ".password is null or empty!");
            }
        }

        boolean useEncoding = Storage.get("Environment.Active.UseEncoding");
        if ( useEncoding && url == null ){
            passwd = StepCore.decodeString(passwd);
        }

        if ( type.equals("jdbc:oracle") ) {
            return createOracleConnection(url, dbIdentifier, host, port, database, user, passwd);
        } else if ( type.equals("jdbc:sqlserver") || type.equals("jdbc:jtds:sqlserver") ) {
            return createMsSqlConnection(url, dbIdentifier, type, host, port, database, instance, user, passwd);
        } else if ( type.equals("jdbc:jtds:sybase") ){
            return createSybaseConnection(url, dbIdentifier, host, port, database, instance, user, passwd);
        } else {
            Log.error("Can't read driver type or wrong name provided. Supported drivers types are: " +
                    "jdbc:oracle, jdbc:sqlserver, jdbc:jtds:sqlserver, jdbc:jtds:sybase");
        }

        return null;
    }

    private Connection getConnection(String classname, URL url,String connectionString, String dbIdentifier){
        try {
            URLClassLoader ucl = new URLClassLoader(new URL[]{url});
            Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
            DriverManager.registerDriver(new JdbcDriverShim(d));
            Log.debug("Connection to " + dbIdentifier + " is open");

            return DriverManager.getConnection(connectionString);

        } catch (ClassNotFoundException | SQLException | IllegalAccessException | InstantiationException e){
            Log.error(e.getMessage());
        }

        return null;
    }

    private Connection createOracleConnection(String userConnectionString, String dbIdentifier, String host, int port, String database, String user, String passwd){
        try {
            Log.debug("Try to load oracle driver");
            String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Oracle.path");
            URL url = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
            String classname = "oracle.jdbc.driver.OracleDriver";

            String connectionString = "jdbc:oracle:thin:" + user + "/" + passwd + "@" + host + ":" + port + "/" + database;
            if ( userConnectionString != null ){
                connectionString = userConnectionString;
            }

            return getConnection(classname, url, connectionString, dbIdentifier);

        } catch (MalformedURLException e) {
            Log.error(e.getMessage());
        }

        return null;
    }

    private Connection createMsSqlConnection(String userConnectionString, String dbIdentifier, String type, String host, int port, String database, String instance, String user, String passwd){
        try {
            Log.debug("Try to load MsSql driver");
            String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Mssql.path");
            URL url = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
            String classname = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

            String connectionString = "jdbc:sqlserver:" + user + "/" + passwd + "@" + host + ":" + port + "/" + database;

            if ( userConnectionString != null ){
                connectionString = userConnectionString;
            }

            if ( userConnectionString == null && type.contains("jdbc:jtds:sqlserver") ){
                classname = "net.sourceforge.jtds.jdbc.Driver";
                if ( (instance == null || instance.equals("")) && (database == null || database.equals("")) ) {
                    connectionString = "jdbc:jtds:sqlserver://" + host + ":" + port + ";user=" + user + ";password=" + passwd;
                } else if ( instance == null || instance.equals("") ){
                    connectionString = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + database + ";user=" + user + ";password=" + passwd;
                } else {
                    connectionString = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + database + ";instance=" + instance + ";user=" + user + ";password=" + passwd;
                }
            }

            return getConnection(classname, url, connectionString, dbIdentifier);

        } catch (MalformedURLException e) {
            Log.error(e.getMessage());
        }

        return null;
    }

    private Connection createSybaseConnection(String userConnectionString, String dbIdentifier, String host, int port, String database, String instance, String user, String passwd){
        try {
            Log.debug("Try to load MsSql driver");
            String pathToDriver = Storage.get("Environment.Active.JdbcDrivers.Sybase.path");
            URL url = new URL("jar:file:" + FileCore.getProjectPath() + File.separator + pathToDriver + "!/");
            String classname = "net.sourceforge.jtds.jdbc.Driver";

            String connectionString;

            if ( userConnectionString != null ){
                connectionString = userConnectionString;
            } else {
                if ( (instance == null || instance.equals("")) && (database == null || database.equals("")) ) {
                    connectionString = "jdbc:jtds:sybase://" + host + ":" + port + ";user=" + user + ";password=" + passwd;
                } else if ( instance == null || instance.equals("") ){
                    connectionString = "jdbc:jtds:sybase://" + host + ":" + port + "/" + database + ";user=" + user + ";password=" + passwd;
                } else {
                    connectionString = "jdbc:jtds:sybase://" + host + ":" + port + "/" + database + ";instance=" + instance + ";user=" + user + ";password=" + passwd;
                }
            }

            return getConnection(classname, url, connectionString, dbIdentifier);

        } catch (MalformedURLException e) {
            Log.error(e.getMessage());
        }

        return null;
    }

}