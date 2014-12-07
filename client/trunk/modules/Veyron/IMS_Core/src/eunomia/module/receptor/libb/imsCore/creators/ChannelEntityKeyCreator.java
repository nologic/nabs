/*
 * ChannelEntityKeyCreator.java
 *
 * Created on January 21, 2008, 1:49 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryMultiKeyCreator;
import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;
import java.sql.PreparedStatement;
import java.util.Set;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelEntityKeyCreator implements SecondaryMultiKeyCreator, EnvironmentKeyCreator, SqlEnvironmentKeyCreator {
    private static String[] colNames;
    private static String[] colTypes;

    static {
        colNames = new String[]{"srcip", "dstip"};
        colTypes = new String[]{"INT UNSIGNED", "INT UNSIGNED"};
    }
    
    private EntryBinding dataBinding;
    private EntryBinding indexKeyBinding;
    
    private DatabaseEntry host1key;
    private DatabaseEntry host2key;
    
    public ChannelEntityKeyCreator() {
    }

    public void createSecondaryKeys(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, Set results) throws DatabaseException {
        host1key = new DatabaseEntry();
        host2key = new DatabaseEntry();

        Object entry = null;
        try {
            entry = dataBinding.entryToObject(data);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        
        if(entry instanceof NetworkChannel) {
            NetworkChannel channel = (NetworkChannel)entry;
            
            indexKeyBinding.objectToEntry(channel.getChannelFlowID().getSourceEntity(), host1key);
            indexKeyBinding.objectToEntry(channel.getChannelFlowID().getDestinationEntity(), host2key);
            
            results.add(host1key);
            results.add(host2key);
        }
    }

    public void setDataBinding(EntryBinding dataBinding) {
        this.dataBinding = dataBinding;
    }

    public void setIndexKeyBinding(EntryBinding indexKeyBinding) {
        this.indexKeyBinding = indexKeyBinding;
    }
    
    public String[] getColumnNames() {
        return colNames;
    }

    public String[] getColumnTypes() {
        return colTypes;
    }

    public void getSqlFieldValue(EnvironmentEntry entry, PreparedStatement s, int f) throws Exception {
        if(entry instanceof NetworkChannel) {
            NetworkChannel channel = (NetworkChannel)entry;
            
            s.setLong(f, channel.getChannelFlowID().getSourceEntity().getIPv4());
            s.setLong(f + 1, channel.getChannelFlowID().getSourceEntity().getIPv4());
            
            return;
        }
        
        throw new RuntimeException("Expected: " + NetworkChannel.class);
    }

    public void getSqlFieldValue(Object key, PreparedStatement s, int f) throws Exception {
        if(key instanceof NetworkEntityHostKey) {
            NetworkEntityHostKey k = (NetworkEntityHostKey)key;
            
            s.setLong(f, k.getIPv4());
            
            return;
        }
        
        throw new RuntimeException("Expected: " + NetworkEntityHostKey.class);
    }
}