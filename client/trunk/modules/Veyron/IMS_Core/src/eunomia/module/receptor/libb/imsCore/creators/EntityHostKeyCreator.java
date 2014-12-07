/*
 * EntityTimeKeyCreator.java
 *
 * Created on January 17, 2008, 8:09 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import java.sql.PreparedStatement;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EntityHostKeyCreator implements SecondaryKeyCreator, EnvironmentKeyCreator, SqlEnvironmentKeyCreator {
    private static String[] colNames;
    private static String[] colTypes;

    static {
        colNames = new String[]{"ip"};
        colTypes = new String[]{"INT UNSIGNED"};
    }
    
    private EntryBinding dataBinding;
    private EntryBinding indexKeyBinding;

    public EntityHostKeyCreator() {
    }

    public boolean createSecondaryKey(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) throws DatabaseException {
        //System.out.println("EntityHostKeyCreator: " + key.getData().length + " " + data.getData().length + " " + data.getPartial());
        Object entry = null;
        try {
            entry = dataBinding.entryToObject(data);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        if(entry instanceof NetworkEntity) {
            NetworkEntity ent = (NetworkEntity)entry;
            NetworkEntityHostKey hkey = ent.getHostKey();
            
            indexKeyBinding.objectToEntry(hkey, result);
            
            return true;
        }
        
        return false;
    }
    
    public void setDataBinding(EntryBinding dataBinding) {
        this.dataBinding = dataBinding;
    }

    public void setIndexKeyBinding(EntryBinding indexKeyBinding) {
        this.indexKeyBinding = indexKeyBinding;
    }

    public void getSqlFieldValue(EnvironmentEntry entry, PreparedStatement s, int f) throws Exception {
        if(entry instanceof NetworkEntity) {
            NetworkEntity ent = (NetworkEntity)entry;
            NetworkEntityHostKey hkey = ent.getHostKey();
            
            s.setLong(f, hkey.getIPv4());
            
            return;
        }
        
        throw new RuntimeException("Bad type");
    }
    
    public void getSqlFieldValue(Object entry, PreparedStatement s, int f) throws Exception {
        if(entry instanceof NetworkEntityHostKey) {
            NetworkEntityHostKey hkey = (NetworkEntityHostKey)entry;
            
            s.setLong(f, hkey.getIPv4());
            
            return;
        }
        
        throw new RuntimeException("Bad type. Required: " + NetworkEntityHostKey.class);
    }
    
    public String[] getColumnNames() {
        return colNames;
    }

    public String[] getColumnTypes() {
        return colTypes;
    }
}