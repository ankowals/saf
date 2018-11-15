package libs.libCore.modules;

import com.opencsv.CSVReader;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class SqlCore {

    private Context scenarioCtx;
    private Context globalCtx;
    private FileCore FileCore;
    private Storage Storage;
    private JdbcDriverObjectPool jdbcDriverObjectPool;

    public SqlCore() {
        this.globalCtx = GlobalCtxSingleton.getInstance();
        this.scenarioCtx = globalCtx.get("ScenarioCtxObjectPool", ScenarioCtxObjectPool.class).checkOut();
        this.jdbcDriverObjectPool = globalCtx.get("JdbcDriverObjectPool", JdbcDriverObjectPool.class);
        this.FileCore = scenarioCtx.get("FileCore",FileCore.class);
        this.Storage = scenarioCtx.get("Storage", Storage.class);
    }


    /**
     * Opens new jdbc connection using params from the configuration Environment.Active.Jdbc
     * and Environment.Active.JdbcDrivers
     *
     */
    private Connection open(String connectionString){
        Log.debug("Opening db connection to " + connectionString);
        return jdbcDriverObjectPool.checkOut(connectionString);
    }


    /**
     * Closes open jdbc connection
     */
    private void close(String connectionString, Connection connection) {
        Log.debug("Closing db connection to " + connectionString);
        jdbcDriverObjectPool.checkIn(connectionString, connection);
    }

    /**
     * Executes sql select statement and returns results in the for of a DataTable (List of Maps)
     *
     * @param SqlQuery String, query to be executed
     * @return List<Map<String,Object>>
     */
    public List<Map<String,Object>> selectList (String connectionString, String SqlQuery) {
        Log.debug("Going to execute Sql query " + SqlQuery);
        //MapListHandler: Multiple rows of data will be returned by the Sql query
        // Each row of data will be encapsulated into a Map,
        // and then stored in the List
        QueryRunner runner = new QueryRunner();
        List<Map<String,Object>> list = null;

        Connection connection = open(connectionString);
        try {
            list = runner.query(connection, SqlQuery, new MapListHandler());
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } finally {
            close(connectionString, connection);
        }

        return list;
    }


    /**
     * Prints result of sql select query to the log file
     *
     * @param list List<Map<String, Object>>, list as returned by method selectList
     */
    public void printList(List<Map<String, Object>> list) {
        Log.debug("Query result is");
        if ( list.size() > 0 ) {
            //print header
            Map<String, Object> firstRow = list.get(0);
            String header = "";
            for (Map.Entry<String, Object> lme : firstRow.entrySet()) {
                header = header + ", " + lme.getKey();
            }
            Log.debug(header.substring(2));

            //print rows
            for (Map<String, Object> map : list) {
                String row = "";
                for (Map.Entry<String, Object> lme : map.entrySet()) {
                    row = row + ", " + lme.getValue();
                }
                Log.debug(row.substring(2));
            }
        }
    }


    /**
     * creates String from the list returned by selectList method
     *
     * @param list List<Map<String, Object>>, list as returned by method selectList
     * @return String
     */
    public String listToString(List<Map<String, Object>> list) {

        String result = "";
        String n = System.lineSeparator();

        if ( list.size() > 0 ) {
            //append header to string
            Map<String, Object> firstRow = list.get(0);
            String header = "";
            for (Map.Entry<String, Object> lme : firstRow.entrySet()) {
                header = header + ", " + lme.getKey();
            }
            result = header + n;

            //append rows to string
            for (Map<String, Object> map : list) {
                String row = "";
                for (Map.Entry<String, Object> lme : map.entrySet()) {
                    row = row + ", " + lme.getValue();
                }
                result = result + row + n;
            }

            return result.trim();
        }

        return result;
    }


    /**
     * writes to file results of the sql select query returned by method selectList
     *
     * @param list List<Map<String, Object>>, list as returned by method selectList
     * @param FileName String, name of the file where results will be stored
     * @param FileExtension String, extension of the file where results will be stored
     *
     * @return File
     */
    public File writeListToFile(List<Map<String, Object>> list, String FileName, String FileExtension) {

        File temp = FileCore.createTempFile(FileName,FileExtension);
        Log.debug("Results stored in " + temp.getAbsolutePath());

        if ( list.size() > 0 ) {
            //write header to file
            Map<String, Object> firstRow = list.get(0);
            String header = "";
            for (Map.Entry<String, Object> lme : firstRow.entrySet()) {
                header = header + ", " + lme.getKey();
            }
            FileCore.writeToFile(temp, header.substring(2) + System.getProperty("line.separator"));

            //append rows to file
            for (Map<String, Object> map : list) {
                String row = "";
                for (Map.Entry<String, Object> lme : map.entrySet()) {
                    row = row + ", " + lme.getValue();
                }
                FileCore.appendToFile(temp, row.substring(2) + System.getProperty("line.separator"));
            }

            return temp;

        } else {
            FileCore.appendToFile(temp, "" + System.getProperty("line.separator"));
        }

        return temp;
    }


    /**
     * Executes sql select statement and returns scalar
     * Shall be used for queries where grouping function are used like count() or sum(), min(), max() etc.
     *
     * @param SqlQuery String, query to be executed
     * @return Integer
     */
    public Integer selectScalar (String connectionString, String SqlQuery) {
        Log.debug("Going to execute Sql query " + SqlQuery);
        //ScalarHandler: Single value of data will be returned by the Sql query
        ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();
        QueryRunner runner = new QueryRunner();
        Integer scalar = null;

        Connection connection = open(connectionString);
        try {
            scalar = runner.query(connection, SqlQuery, scalarHandler);
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } finally {
            close(connectionString, connection);
        }

        return scalar;
    }


    /**
     * Executes sql insert statement
     *
     * @param SqlQuery String, query to be executed
     * @return Integer
     */
    public void insert (String connectionString, String SqlQuery) {
        Log.debug("Going to execute Sql " + SqlQuery);
        QueryRunner runner = new QueryRunner();

        Connection connection = open(connectionString);
        try {
            runner.insert(connection, SqlQuery, new ScalarHandler<>());
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } finally {
            close(connectionString, connection);
        }

    }


    /**
     * Executes sql update statement and returns scalar indicating how many rows where changed
     * in the table
     *
     * @param SqlQuery String, query to be executed
     * @return Integer
     */
    public Integer update (String connectionString, String SqlQuery) {
        Log.debug("Going to execute Sql " + SqlQuery);
        Integer scalar = 0;
        QueryRunner runner = new QueryRunner();

        Connection connection = open(connectionString);
        try {
            scalar = runner.update(connection, SqlQuery);
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } finally {
            close(connectionString, connection);
        }

        return scalar;
    }


    /**
     * Executes sql delete statement
     *
     * @param SqlQuery String, query to be executed
     * @return Integer
     */
    public Integer delete (String connectionString, String SqlQuery) {
        Log.debug("Going to execute Sql " + SqlQuery);
        Integer scalar = 0;
        QueryRunner runner = new QueryRunner();

        Connection connection = open(connectionString);
        try {
            scalar = runner.update(connection, SqlQuery);
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } finally {
            close(connectionString, connection);
        }

        return scalar;
    }


    /**
     * Executes sql insert statement but takes data from csv file and column types
     * from configuration Storage
     * Optionaly can truncate table data before insert will be done
     *
     * @param file File, csv file handle with data to insert
     * @param tableName String, name of the table to which data shall be inserted
     * @param truncateBeforeLoad boolean, switch to truncate data before insert
     * @param typeMapping String, name of the Storage with mapping of data types in the columns (shall be List<String>)
     */
    public void insertFromFile(String connectionString, File file, String tableName, boolean truncateBeforeLoad, String typeMapping) {
        String SQL_INSERT = "INSERT INTO ${table}(${keys}) VALUES(${values})";
        String TABLE_REGEX = "\\$\\{table\\}";
        String KEYS_REGEX = "\\$\\{keys\\}";
        String VALUES_REGEX = "\\$\\{values\\}";
        Character separator = ',';
        CSVReader csvReader = null;
        String[] headerRow = null;

        try {
            csvReader = new CSVReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            Log.error(e.getMessage());
        }

        try {
            headerRow = csvReader.readNext();
        } catch (IOException e) {
            Log.error(e.getMessage());
        }

        if ( null == headerRow ) {
            Log.error( "No columns defined in given CSV file." +
                    "Please check the CSV file format.");
        }

        String questionmarks = StringUtils.repeat("?,", headerRow.length);
        questionmarks = (String) questionmarks.subSequence(0, questionmarks.length() - 1);

        String query = SQL_INSERT.replaceFirst(TABLE_REGEX, tableName);
        query = query.replaceFirst(KEYS_REGEX, StringUtils.join(headerRow, ","));
        query = query.replaceFirst(VALUES_REGEX, questionmarks);

        Log.debug("Sql query: " + query);

        String[] nextLine;
        PreparedStatement ps = null;

        Connection connection = open(connectionString);
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(query);

            if(truncateBeforeLoad) {
                //delete data from table before loading csv
                connection.createStatement().execute("DELETE FROM " + tableName);
            }

            final int batchSize = 1000;
            int count = 0;
            try {
                Log.debug("Reading csv file");
                while ((nextLine = csvReader.readNext()) != null) {

                    if (null != nextLine) {

                        List<String> colToTypeList = Storage.get(typeMapping);
                        if ( colToTypeList == null ) {
                            Log.error("Type mapping " + typeMapping + " null!");
                        }
                        if ( colToTypeList.size() < 1 ) {
                            Log.error("Type mapping " + typeMapping + " empty!");
                        }
                        if ( colToTypeList.size() != nextLine.length ) {
                            Log.error("Number of columns in the file (" + nextLine.length +
                                    ") and in type mapping (" + colToTypeList.size() +
                                    ") is not equal");
                        }

                        int index = 1;
                        for (String string : nextLine) {
                            int idx = index -1;
                            //type mapping
                            if ( Storage.get(typeMapping+"["+idx+"]").equals("NUMERIC") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("DECIMAL") ){
                                BigDecimal number = new BigDecimal(string);
                                ps.setBigDecimal(index++, number);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("VARCHAR") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("CHARACTER") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("LONGVARCHAR") ){
                                ps.setString(index++, string);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("BIT") ) {
                                Boolean b = BooleanUtils.toBoolean(string);
                                ps.setBoolean(index++, b);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("TINYINT") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("SMALLINT") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("INTEGER") ) {
                                Integer number = Integer.getInteger(string);
                                ps.setInt(index++, number);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("BIGINT") ) {
                                Long number = Long.getLong(string);
                                ps.setLong(index++, number);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("REAL") ) {
                                Float number = Float.parseFloat(string);
                                ps.setFloat(index++, number);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("FLOAT") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("DOUBLE PRECISION") ) {
                                Double number = Double.valueOf(string);
                                ps.setDouble(index++, number);
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("BINARY") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("VARBINARY") ||
                                    Storage.get(typeMapping+"["+idx+"]").equals("LONGVARBINARY") ){
                                Log.error("Wrong type provided. " + "" +
                                        "BINARY, VARBINARY and LONGVARBINARY are not supported");
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("DATE") ) {
                                    Date date = DateParser.convertToDate(string);
                                    ps.setDate(index++, new java.sql.Date(date.getTime()));
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("TIME") ) {
                                Pattern p = Pattern.compile("\\d\\d:\\d\\d:\\d\\d"); // not perfect but good enough;)
                                Matcher m = p.matcher(string);
                                if (m.matches()) {
                                    Time time = Time.valueOf(string);
                                    ps.setTime(index++, time);
                                } else {
                                    Log.error("Wrong time format provided. Expected is hh:mm:ss" +
                                    " but was " + string);
                                }
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("TIMESTAMP") ) {
                                try {
                                    Timestamp timestamp = Timestamp.valueOf(string);
                                    ps.setTimestamp(index++, timestamp);
                                } catch ( IllegalArgumentException e) {
                                    Log.error("Wrong timestamp format provided! " + e.getMessage());
                                }
                            } else {
                                Log.error("Wrong type provided. Type in typeMapping[" + idx + "] not known");
                            }
                        }
                        ps.addBatch();
                    }
                    if (++count % batchSize == 0) {
                        ps.executeBatch();
                    }
                }
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
            ps.executeBatch(); // insert remaining records
            connection.commit();
            Log.debug("Sql batch query executed");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                Log.error("SQL batch query rollback execution failed! " + e1.getMessage() );
            }
            Log.error(e.getMessage());
        } finally {
            if (null != ps)
                try {
                    ps.close();
                } catch (SQLException e) {
                    Log.error(e.getMessage());
                }
            try {
                csvReader.close();
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
            close(connectionString, connection);
        }
    }

}