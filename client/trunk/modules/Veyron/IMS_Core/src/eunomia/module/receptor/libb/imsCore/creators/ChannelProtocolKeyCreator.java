/*
 * ChannelProtocolKeyCreator.java
 *
 * Created on February 3, 2008, 6:08 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.ByteBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import java.sql.PreparedStatement;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelProtocolKeyCreator implements SecondaryKeyCreator, EnvironmentKeyCreator, SqlEnvironmentKeyCreator {
    private static String[] colNames;
    private static String[] colTypes;

    static {
        colNames = new String[]{"proto"};
        colTypes = new String[]{"SMALLINT UNSIGNED"};
    }
    
    private EntryBinding dataBinding;
    private ByteBinding indexKeyBinding;
    
    public ChannelProtocolKeyCreator() {
    }

    public boolean createSecondaryKey(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) throws DatabaseException {
        Object entry = dataBinding.entryToObject(data);
        
        if(entry instanceof NetworkChannel) {
            NetworkChannel ent = (NetworkChannel)entry;
            
            indexKeyBinding.byteToEntry((byte)ent.getChannelFlowID().getProtocol(), result);
            
            return true;
        }
        
        return false;
    }

    public void setDataBinding(EntryBinding dataBinding) {
        this.dataBinding = dataBinding;
    }

    public void setIndexKeyBinding(EntryBinding indexKeyBinding) {
        this.indexKeyBinding = (ByteBinding) indexKeyBinding;
    }

    public void getSqlFieldValue(EnvironmentEntry entry, PreparedStatement s, int f) throws Exception {
        if(entry instanceof NetworkChannel) {
            NetworkChannel ent = (NetworkChannel)entry;
            int p = ent.getChannelFlowID().getProtocol();
            
            s.setByte(f, (byte)p);
            
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