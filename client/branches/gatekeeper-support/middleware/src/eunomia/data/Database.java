/*
 * Database.java
 *
 * Created on June 2, 2005, 12:48 PM
 */

package eunomia.data;

import eunomia.config.Config;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.managers.ModuleManager;
import eunomia.messages.DatabaseDescriptor;
import eunomia.plugin.interfaces.CollectionModule;
import eunomia.util.Util;

import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author  Mikhail Sosonkin
 */
public abstract class Database {
    private int serial;
    
    protected String address;
    private String jdbcType;
    protected int dbPort;
    protected String dbName;
    protected String tableName;
    protected String username;
    protected String password;
    protected Connection dbConnection;
    protected Statement stmt;
    protected String name; 
    protected Map collectors;
    protected boolean isConnected;
    
    public Database() throws Exception {
        this(null);
    }
    
    public Database(String n) throws Exception  {
        collectors = new HashMap();
        serial = Util.getRandomInt(null);
        name = n;
        
        dbPort = 3306;
        tableName = "flows";
        dbName = "nabs";
        address = "127.0.0.1";
        username = "naber";
        password = "stream";
    }
    
    // All collection module stuff should be moved outside of Database class.
    public FlowProcessor getCollector(String coll) throws Exception {
        CollectionModule module = (CollectionModule)collectors.get(coll);
        
        if(module == null) {
            module = ModuleManager.v().startModule_COLL(coll);
            collectors.put(coll, module);
            module.setDatabase(this);
        }
        
        return module.getFlowProcessor();
    }
    
    public String[] getCollectors() {
        return (String[])collectors.keySet().toArray(new String[collectors.size()]);
    }
    
    public void removeCollector(String coll) {
        CollectionModule module = (CollectionModule)collectors.get(coll);
        
        if(module != null){
            module.destroy();
            collectors.remove(coll);
        }
    }
    
    public boolean isCollecting(String coll){
        return collectors.get(coll) != null;
    }

    public boolean isConnected() {
        return isConnected;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public Connection getConnection(){
        return dbConnection;
    }
    
    public void setCredentials(String user, String pass){
        username = user;
        if(pass != null){
            password = pass;
        }
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
    
    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
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
    
    public DatabaseDescriptor getDescriptor(boolean includePass){
        DatabaseDescriptor dbDesc = new DatabaseDescriptor();
        
        dbDesc.setName(name);
        dbDesc.setAddress(address);
        dbDesc.setDbName(dbName);
        dbDesc.setDbType(jdbcType);
        if(includePass){
            dbDesc.setPassword(password);
        }
        dbDesc.setPort(dbPort);
        dbDesc.setTableName(tableName);
        dbDesc.setUsername(username);
        dbDesc.setConnected(isConnected);
        Iterator it = collectors.keySet().iterator();
        while (it.hasNext()) {
            dbDesc.addCollector(it.next().toString());
        }
        
        return dbDesc;
    }

    public void save() {
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
    
    public void load() {
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