/*
 * JDBCDatabase.java
 *
 * Created on December 20, 2006, 10:39 PM
 *
 */

package eunomia.data.relational;

import eunomia.data.Database;
import eunomia.managers.DatabaseManager;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Mikhail Sosonkin
 */
public class JDBCDatabase extends Database {
    public JDBCDatabase() throws Exception {
        this(null);
    }
    
    public JDBCDatabase(String name) throws Exception {
        super(name);
        isConnected = false;
    }
    
    public void connect() throws SQLException {
        if(!isConnected()){
            String url = "jdbc:" + jdbcType + "://" + address + ":" + dbPort + "/" + dbName;
            
            Properties info = new Properties();
            info.put("user", username);
            info.put("password", password);
            
            Driver driver = DatabaseManager.v().getDriver(jdbcType);
            dbConnection = driver.connect(url, info);

            stmt = dbConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            isConnected = true;
        }
    }
    
    public void disconnect() throws SQLException {
        if(isConnected()){
            stmt.close();
            dbConnection.close();
            isConnected = false;
        }
    }
}