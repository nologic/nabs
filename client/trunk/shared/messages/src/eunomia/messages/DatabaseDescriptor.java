/*
 * DatabaseDescriptor.java
 *
 * Created on February 7, 2006, 9:22 PM
 *
 */

package eunomia.messages;

import java.io.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseDescriptor implements Externalizable {
    private static final long serialVersionUID = 4118401206217342160L;
    
    public static final String DB_VOID_TYPE = new String("-");
    
    private int hash;
    private Set collectors;
    private String name;
    private String dbType;
    private String address;
    private String dbName;
    private String tableName;
    private String username;
    private String password;
    private int port;
    private boolean isConnected;
    
    public DatabaseDescriptor() {
        collectors = new HashSet();
    }

    public String toString(){
        return name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbT) {
        this.dbType = dbT;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public Set getCollectors() {
        return collectors;
    }

    public void addCollector(String collector) {
        collectors.add(collector);
    }
    
    public void removeCollector(String collector) {
        collectors.remove(collector);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dbType);
        out.writeObject(name);
        out.writeObject(address);
        out.writeObject(dbName);
        out.writeObject(tableName);
        out.writeObject(username);
        out.writeBoolean(password != null);
        if(password != null){
            out.writeObject(password);
        }
        out.writeInt(port);
        out.writeBoolean(isConnected);
        
        out.writeInt(collectors.size());
        Iterator it = collectors.iterator();
        while (it.hasNext()) {
            out.writeObject(it.next());
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        dbType = (String)in.readObject();
        name = (String)in.readObject();
        address = (String)in.readObject();
        dbName = (String)in.readObject();
        tableName = (String)in.readObject();
        username = (String)in.readObject();
        if(in.readBoolean()){
            password = (String)in.readObject();
        }
        port = in.readInt();
        isConnected = in.readBoolean();
        
        int len = in.readInt();
        for (int i = 0; i < len; i++) {
            collectors.add(in.readObject());
        }
    }
}