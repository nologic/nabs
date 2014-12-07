/*
 * Database.java
 *
 * Created on June 2, 2005, 12:48 PM
 */

package eunomia.core.data.staticData;

import java.io.*;
import java.sql.*;
import java.net.*;

import eunomia.*;
import eunomia.config.*;
import eunomia.core.data.streamData.client.listeners.*;
import eunomia.util.Util;

/**
 *
 * @author  Mikhail Sosonkin
 */
public abstract class Database {
    private int serial;
    
    protected String address;
    protected int dbPort;
    protected String dbName;
    protected String tableName;
    protected String username;
    protected String password;
    protected Connection dbConnection;
    protected Statement stmt;
    protected String name; 
    protected Collector collector;
    
    public Database() throws Exception {
        this(null);
    }
    
    public Database(String n) throws Exception  {
        serial = Util.getRandomInt(null);
        name = n;
        
        dbPort = 3306;
        tableName = "flows";
        dbName = "nabs";
        address = "127.0.0.1";
        username = "naber";
        password = "stream";
    }
    
    public FlowProcessor getCollector() throws Exception {
        if(collector == null){
            collector = new Collector(getNewStatement(), getMainTable());
        }
        
        return collector;
    }
    
    public String getName(){
        return name;
    }
    
    public Connection getConnection(){
        return dbConnection;
    }
    
    public void setCredentials(String user, String pass){
        username = user;
        password = pass;
    }
    
    public String getUserName(){
        return username;
    }
    
    public String getPassword(){
        return password;
    }
    
    public void setPort(int p){
        dbPort = p;
    }
    
    public int getPort(){
        return dbPort;
    }
    
    public int getSerial(){
        return serial;
    }
    
    public void setMainTable(String table){
        tableName = table;
    }
    
    public String getMainTable(){
        return tableName;
    }
    
    public void setDatabaseName(String name){
        dbName = name;
    }
    
    public String getDatabaseName(){
        return dbName;
    }

    public void setAddress(InetAddress add){
        address = add.getHostName();
    }
    
    public void setAddress(String add) throws UnknownHostException {
        address = add;
    }
    
    public String getAddress(){
        return address;
    }
    
    public boolean execute(String query) throws SQLException {
        if(stmt == null){
            throw new NullPointerException("Current 'statement' is null");
        }

        return stmt.execute(query);
    }
    
    public int executeUpdate(String query) throws SQLException {
        if(stmt == null){
            throw new NullPointerException("Current 'statement' is null");
        }

        return stmt.executeUpdate(query);
    }
    
    public ResultSet executeQuery(String query) throws SQLException {
        if(stmt == null){
            throw new NullPointerException("Current 'statement' is null");
        }

        return stmt.executeQuery(query);
    }
    
    public Statement getCurrentStatement(){
        return stmt;
    }
    
    public Statement getNewStatement(int type, int concurrency) throws SQLException {
        return dbConnection.createStatement(type, concurrency);
    }
    
    public Statement getNewStatement() throws SQLException {
        return dbConnection.createStatement();
    }

    public String toString(){
        return getName();
    }

    public void save() throws IOException {
        Config config = Config.getConfiguration("databases." + name);
        
        config.setInt("serial", serial);
        config.setString("address", address);
        config.setInt("dbPort", dbPort);
        config.setString("dbName", dbName);
        config.setString("tableName", tableName);
        config.setString("username", username);
        config.setString("password", password);
        
        config.save();
    }
    
    public void load() throws IOException {
        Config config = Config.getConfiguration("databases." + name);
        
        address = config.getString("address", null);
        serial = config.getInt("serial", -1);
        dbPort = config.getInt("dbPort", -1);
        dbName = config.getString("dbName", null);
        tableName = config.getString("tableName", null);
        username = config.getString("username", null);
        password = config.getString("password", null);
    }
    
    public abstract void connect() throws SQLException;
    public abstract void disconnect() throws SQLException;
}