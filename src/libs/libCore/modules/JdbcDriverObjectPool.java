package libs.libCore.modules;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class JdbcDriverObjectPool extends AbstractObjectPool<Connection>{

    JdbcDriverObjectPool(){
        this.available = new HashMap<>();
        this.inUse = new HashMap<>();
    }

    @Override
    protected Connection create(String connectionString){
        return new JdbcDriverFactory().create(connectionString);
    }

    @Override
    protected void close(Connection instance) {
        if ( validate(instance) ){
            try {
                instance.close();
            } catch (SQLException e) {
                Log.error(e.getMessage());
            }
        }
    }

}