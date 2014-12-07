/*
 * EntityTimeKeyCreator.java
 *
 * Created on January 17, 2008, 8:09 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.db.NetworkInserter;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import java.sql.PreparedStatement;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelStartTimeKeyCreator implements SecondaryKeyCreator, EnvironmentKeyCreator, SqlEnvironmentKeyCreator {
    private static String[] colNames;
    private static String[] colTypes;

    static {
        colNames = new String[]{"startAct"};
        colTypes = new String[]{"INT UNSIGNED"};
    }
    
    private EntryBinding dataBinding;
    private LongBinding indexKeyBinding;
    
    public ChannelStartTimeKeyCreator() {
    }

    public boolean createSecondaryKey(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) throws DatabaseException {
        Object entry = dataBinding.entryToObject(data);
        
        if(entry instanceof NetworkChannel) {
            NetworkChannel ent = (NetworkChannel)entry;
            long tenmin = ent.getStartTime().getSeconds() >> NetworkInserter.TIME_SHIFT;

            indexKeyBinding.longToEntry(tenmin, result);
            
            return true;
        }
        
        return false;
    }

    public void setDataBinding(EntryBinding dataBinding) {
        this.dataBinding = dataBinding;
    }

    public void setIndexKeyBinding(EntryBinding indexKeyBinding) {
        this.indexKeyBinding = (LongBinding) indexKeyBinding;
    }

    public void getSqlFieldValue(EnvironmentEntry entry, PreparedStatement s, int f) throws Exception {
        if(entry instanceof NetworkChannel) {
            NetworkChannel ent = (NetworkChannel)entry;
            long tenmin = ent.getStartTime().getSeconds() >> NetworkInserter.TIME_SHIFT;
            
            s.setLong(f, tenmin);
            
            return;
        }
        
        throw new RuntimeException("Expected type: " + NetworkChannel.class);
    }
    
    public void getSqlFieldValue(Object entry, PreparedStatement s, int f) throws Exception {
        throw new UnsupportedOperationException();
    }
    
    public String[] getColumnNames() {
        return colNames;
    }

    public String[] getColumnTypes() {
        return colTypes;
    }
}