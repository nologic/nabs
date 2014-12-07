/*
 * JDBCDatabase.java
 *
 * Created on December 20, 2006, 10:39 PM
 *
 */

package eunomia.data.db;

import eunomia.data.Database;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Mikhail Sosonkin
 */
public class JDBCDatabase extends Database {
    private String type;
    
    public JDBCDatabase() throws Exception {
        this(null);
    }
    
    public JDBCDatabase(String name) throws Exception {
        super(name);
        isConnected = false;
    }
    
    public void connect() throws SQLException {
        if(!isConnected()){
            String url = "jdbc:" + type + "://" + address + ":" + dbPort + "/" + dbName;
            System.out.println(url);
            dbConnection = DriverManager.getConnection(url, username, password);

            stmt = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
        }
    }
    
    public void disconnect() throws SQLException {
        if(isConnected()){
            stmt.close();
            dbConnection.close();
        }
    }
    
    public void setJdbcType(String t){
        super.setJdbcType(t);
        type = t;
    }
}