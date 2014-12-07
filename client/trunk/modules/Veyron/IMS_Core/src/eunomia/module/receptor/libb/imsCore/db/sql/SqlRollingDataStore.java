/*
 * SqlRollingDataStore.java
 *
 * Created on June 7, 2008, 3:36 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db.sql;

import com.sleepycat.db.DatabaseException;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.bind.SerialObjectBinding;
import eunomia.module.receptor.libb.imsCore.creators.SqlEnvironmentKeyCreator;
import eunomia.module.receptor.libb.imsCore.db.*;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SqlRollingDataStore implements RollingDatabaseStore {
    private StoreDescriptor[] descriptors;
    private Connection wcon;
    private Connection rcon;
    private Statement gStat;
    private String name;
    private String curTableName;
    
    private int dbNum;
    private int oldestDbNum;
    private int max_db;
    
    public SqlRollingDataStore(String name, StoreDescriptor[] descriptors, int db_count) throws Exception {
        this.name = name;
        this.descriptors = descriptors;
        this.max_db = db_count;
        
        Driver driver = new com.mysql.jdbc.Driver();
        String url = "jdbc:mysql://127.0.0.1:3306/veyron";
        
        Properties info = new Properties();
        info.put("user", "veyron");
        info.put("password", "veyron");
        
        wcon = driver.connect(url, info);
        rcon = driver.connect(url, info);
        gStat = wcon.createStatement();
        
        StringBuilder b = new StringBuilder("CREATE TABLE ").append(name).append("_sample (");
        
        // make sample table.
        for (int i = 0; i < descriptors.length; ++i) {
            StoreDescriptor d = descriptors[i];
            SqlEnvironmentKeyCreator kc = (SqlEnvironmentKeyCreator) d.getKeyCreator();
            String[] fields;
            String[] types;
            
            if(kc != null) {
                fields = kc.getColumnNames();
                types = kc.getColumnTypes();
            } else {
                fields = new String[]{name};
                types = new String[]{"BLOB"};
            }
            
            for (int j = 0; j < fields.length; ++j) {
                b.append(fields[j]).append(" ").append(types[j]);
                
                if(i > 0) {
                    b.append(", INDEX USING HASH (").append(fields[j]).append(")");
                }
                
                if(j < fields.length - 1) {
                    b.append(",");
                }
            }

            if(i < descriptors.length - 1) {
                b.append(",");
            }
        }
        b.append(")");
        
        gStat.execute("DROP TABLE IF EXISTS " + name + "_sample");
        gStat.execute(b.toString());
        
        roll(null);
    }
    
    public void roll(String prefix) {
        curTableName = name + dbNum;
        try {
            gStat.execute("DROP TABLE IF EXISTS " + curTableName);
            gStat.execute("CREATE TABLE " + curTableName + " LIKE " + name + "_sample");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        dbNum++;
    }

    public void putArray(EnvironmentEntry[] arr, int offset, int length) throws DatabaseException {
        /*System.out.println("Putting (" + name + ": " + arr.length);
        long time = System.currentTimeMillis();*/
        
        StringBuilder b = new StringBuilder("INSERT INTO ");
        b.append(curTableName).append(" VALUES (");
        for (int i = 0; i < descriptors.length; ++i) {
            StoreDescriptor d = descriptors[i];
            SqlEnvironmentKeyCreator kc = (SqlEnvironmentKeyCreator) d.getKeyCreator();
            
            String[] fields;
            if(kc == null) {
                fields = new String[]{name};
            } else {
                fields = kc.getColumnNames();
            }
            
            for (int j = 0; j < fields.length; ++j) {
                b.append("?");
                
                if(j < fields.length - 1) {
                    b.append(",");
                }
            }
            
            if(i < descriptors.length - 1) {
                b.append(",");
            }
        }
        b.append(")");
        
        try {
            PreparedStatement s = wcon.prepareStatement(b.toString());
            for (int i = 0; i < length; ++i) {
                EnvironmentEntry e = arr[offset + i];

                for (int j = 0, f = 1; j < descriptors.length; ++j) {
                    StoreDescriptor d = descriptors[j];
                    
                    if(j == 0) {
                        // yes, I know...
                        //s.setBlob(f, new ByteArrayInputStream(((SerialObjectBinding)d.getValueBinding()).getBuffer(e)));
                        byte[] bytes = ((SerialObjectBinding)d.getValueBinding()).getBuffer(e);
                        s.setBytes(f, bytes);
                        f++;
                    } else {
                        SqlEnvironmentKeyCreator kc = (SqlEnvironmentKeyCreator)d.getKeyCreator();
                        kc.getSqlFieldValue(e, s, f);
                        f += kc.getColumnNames().length;
                    }
                }
                
                s.executeUpdate();
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        /*time = System.currentTimeMillis() - time;
        System.out.println(time);
        System.out.println( ((double)time)/(double)arr.length);*/
    }

    public DataStoredMap getMap(int map, List dbs) {
        if(dbNum <= oldestDbNum) {
            return null;
        }
        
        String[] tables = new String[dbNum - oldestDbNum - 1];
        for (int i = 0; i < tables.length; ++i) {
            tables[i] = name + (oldestDbNum + i);
        }

        try {
            SQLStoredMap s = new SQLStoredMap(rcon, tables, name, 
                                                (SerialObjectBinding) descriptors[map].getValueBinding(), 
                                                (SqlEnvironmentKeyCreator) descriptors[map].getKeyCreator());
            return s;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    public void close() throws DatabaseException {
        try {
            gStat.close();
            wcon.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Object getEntry(Object key) {
        return null;
    }

    public List getDatabases() {
        return null;
    }
}