package modules.core;

import com.opencsv.CSVReader;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SqlCore {

    private SharedContext ctx;
    private FileCore FileCore;
    private Connection Sql;
    private Storage Storage;

    // PicoContainer injects class SharedContext
    public SqlCore(SharedContext ctx) {
        this.ctx = ctx;
        this.FileCore = ctx.Object.get("FileCore",FileCore.class);
        this.Sql = ctx.Object.get("Sql",Connection.class);
        this.Storage = ctx.Object.get("Storage", Storage.class);
    }

    public List<Map<String,Object>> selectList (String SqlQuery) {

        Log.debug("Going to execute Sql query " + SqlQuery);
        //MapListHandler: Multiple rows of data will be returned by the Sql query
        // Each row of data will be encapsulated into a Map,
        // and then stored in the List
        QueryRunner runner = new QueryRunner();
        List<Map<String,Object>> list = null;

        try {
            list = runner.query(Sql, SqlQuery, new MapListHandler());
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error( "", e );
        }

        return list;
    }

    public void printList(List<Map<String, Object>> list) {

        //print header
        Map<String, Object> firstRow = list.get(0);
        String header = "";
        for (Map.Entry<String, Object> lme : firstRow.entrySet()) {
            header = header + ", " + lme.getKey();
        }
        Log.debug(header.substring(2));

        //print rows
        for(Map<String,Object> map:list){
            String row = "";
            for(Map.Entry<String,Object> lme : map.entrySet()){
                row = row + ", " + lme.getValue();
            }
            Log.debug(row.substring(2));
        }
    }

    public String listToString(List<Map<String, Object>> list) {

        String result;
        String n = System.lineSeparator();

        //append header to string
        Map<String, Object> firstRow = list.get(0);
        String header = "";
        for (Map.Entry<String, Object> lme : firstRow.entrySet()) {
            header = header + ", " + lme.getKey();
        }
        result = header + n;

        //append rows to string
        for(Map<String,Object> map:list){
            String row = "";
            for(Map.Entry<String,Object> lme : map.entrySet()){
                row = row + ", " + lme.getValue();
            }
            result = result + row + n;
        }

        return result.trim();
    }

    public File writeListToFile(List<Map<String, Object>> list, String FileName, String FileExtension) {

        File temp = FileCore.createTempFile(FileName,FileExtension);
        Log.debug("Results stored in " + temp.getAbsolutePath());

        //write header to file
        Map<String, Object> firstRow = list.get(0);
        String header = "";
        for (Map.Entry<String, Object> lme : firstRow.entrySet()) {
            header = header + ", " + lme.getKey();
        }
        FileCore.writeToFile(temp,header.substring(2) + System.getProperty("line.separator"));

        //append rows to file
        for(Map<String,Object> map:list){
            String row = "";
            for(Map.Entry<String,Object> lme : map.entrySet()){
                row = row + ", " + lme.getValue();
            }
            FileCore.appendToFile(temp,row.substring(2) + System.getProperty("line.separator"));
        }

        return temp;
    }

    public <T> ScalarHandler selectScalar (String SqlQuery) {

        Log.debug("Going to execute Sql query " + SqlQuery);
        //ScalarHandler: Single value of data will be returned by the Sql query
        QueryRunner runner = new QueryRunner();
        ScalarHandler<T> scalar = null;

        try {
            scalar = runner.query(Sql, SqlQuery, new ScalarHandler<>());
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error("", e );
        }

        return scalar;
    }

    public <T> ScalarHandler insert (String SqlQuery) {

        Log.debug("Going to execute Sql insert " + SqlQuery);
        ScalarHandler<T> scalar = null;
        QueryRunner runner = new QueryRunner();

        try {
            scalar = runner.insert(Sql, SqlQuery, new ScalarHandler<>());
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error( "", e );
        }

        return scalar;
    }

    public Integer update (String SqlQuery) {

        Log.debug("Going to execute Sql update " + SqlQuery);
        Integer scalar = 0;
        QueryRunner runner = new QueryRunner();

        try {
            scalar = runner.update(Sql, SqlQuery);
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error( "", e );
        }

        return scalar;
    }

    public Integer delete (String SqlQuery) {

        Log.debug("Going to execute Sql delete " + SqlQuery);
        Integer scalar = 0;
        QueryRunner runner = new QueryRunner();

        try {
            scalar = runner.update(Sql, SqlQuery);
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            Log.error( "", e );
        }

        return scalar;
    }

    public void insertFromFile(File file, String tableName, boolean truncateBeforeLoad, String typeMapping) {
        String SQL_INSERT = "INSERT INTO ${table}(${keys}) VALUES(${values})";
        String TABLE_REGEX = "\\$\\{table\\}";
        String KEYS_REGEX = "\\$\\{keys\\}";
        String VALUES_REGEX = "\\$\\{values\\}";
        Character seprator = ',';
        CSVReader csvReader = null;
        String[] headerRow = null;

        try {
            csvReader = new CSVReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            Log.error("", e);
        }

        try {
            headerRow = csvReader.readNext();
        } catch (IOException e) {
            Log.error( "", e );
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

        try {
            Sql.setAutoCommit(false);
            ps = Sql.prepareStatement(query);

            if(truncateBeforeLoad) {
                //delete data from table before loading csv
                Sql.createStatement().execute("DELETE FROM " + tableName);
            }

            final int batchSize = 1000;
            int count = 0;
            Date date = null;
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
                                    //date = DateUtil.convertToDate(string);
                                    //if (null != date) {
                                    //  ps.setDate(index++, new java.sql.Date(date.getTime()));
                                Log.error("Wrong type provided. DATE not yet supported");
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("TIME") ) {
                                Log.error("Wrong type provided. TIME not yet supported");
                            } else if ( Storage.get(typeMapping+"["+idx+"]").equals("TIMESTAMP") ) {
                                Log.error("Wrong type provided. TIMESTAMP not yet supported");
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
                Log.error( "", e );
            }
            ps.executeBatch(); // insert remaining records
            Sql.commit();
            Log.debug("Sql query executed");
        } catch (SQLException e) {
            try {
                Sql.rollback();
            } catch (SQLException e1) {
                Log.error( "SQL query rollback execution failed", e1 );
            }
            Log.error( "", e );
        } finally {
            if (null != ps)
                try {
                    ps.close();
                } catch (SQLException e) {
                    Log.error( "", e );
                }
            try {
                csvReader.close();
            } catch (IOException e) {
                Log.error( "", e );
            }
        }
    }

}
