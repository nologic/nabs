/*
 * MySQLDatabase.java
 *
 * Created on June 2, 2005, 12:57 PM
 */

package eunomia.data.db;

import eunomia.data.Database;
import java.sql.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class MySQLDatabase extends Database {
    public MySQLDatabase() throws Exception {
        this(null);
    }
    
    public MySQLDatabase(String name) throws Exception {
        super(name);
        isConnected = false;
        //Class.forName("com.mysql.jdbc.Driver");
    }
    
    public void connect() throws SQLException {
        if(!isConnected()){
            dbConnection = DriverManager.getConnection("jdbc:mysql://" + 
                address + ":" + dbPort + "/" + dbName, username, password);

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
}