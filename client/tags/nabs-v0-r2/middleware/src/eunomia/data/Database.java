/*
 * Database.java
 *
 * Created on June 2, 2005, 12:48 PM
 */

package eunomia.data;

import eunomia.config.Config;
import eunomia.data.collection.Collector;
import eunomia.flow.FlowProcessor;
import eunomia.messages.DatabaseDescriptor;
import eunomia.util.Util;

import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;



/**
 *
 * @author  Mikhail Sosonkin
 */
public abstract class Database {
    private int serial;
    private ThreadGroup analGroup;
    private Set analThreads;
    
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
    protected Collector collector;
    protected boolean isConnected;
    private boolean isCollecting;
    
    public Database() throws Exception {
        this(null);
    }
    
    public Database(String n) throws Exception  {
        analThreads = new HashSet();
        serial = Util.getRandomInt(null);
        name = n;
        analGroup = new ThreadGroup("Analysis Thread Group for: " + n);
        analGroup.setMaxPriority(Thread.MIN_PRIORITY);
        
        dbPort = 3306;
        tableName = "flows";
        dbName = "nabs";
        address = "127.0.0.1";
        username = "naber";
        password = "stream";
    }
    
    public void addAnalysisThread(AnalysisThread thread){
        analThreads.add(thread);
    }
    
    public void removeAnalysisThread(AnalysisThread thread) {
        analThreads.remove(thread);
    }
    
    public Set getAnalysisThreads(){
        return analThreads;
    }
    
    public ThreadGroup getThreadGroup() {
        return analGroup;
    }
    
    public AnalysisThread[] getThreadList() {
        Thread[] list = new Thread[analGroup.activeCount()];
        
        analGroup.enumerate(list, false);
        
        return (AnalysisThread[])list;
    }
    
    public FlowProcessor getCollector() throws Exception {
        collector = new Collector(getNewStatement(), getMainTable());
        return collector;
    }
    
    public boolean isConnected() {
        return isConnected;
    }

    public boolean isCollecting() {
        return isCollecting;
    }

    public void setCollecting(boolean isCollecting) {
        if(!isCollecting){
            collector.quit();
            collector = null;
        }
        
        this.isCollecting = isCollecting;
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
        dbDesc.setCollecting(isCollecting);
        dbDesc.setConnected(isConnected);
        
        return dbDesc;
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