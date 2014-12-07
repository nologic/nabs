/*
 * ChannelFlowIDKeyCreator.java
 *
 * Created on January 26, 2008, 6:09 PM
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
import eunomia.module.receptor.libb.imsCore.net.NetworkChannelFlowID;
import java.sql.PreparedStatement;
import java.util.Set;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChannelFlowIDKeyCreator implements SecondaryMultiKeyCreator, EnvironmentKeyCreator, SqlEnvironmentKeyCreator {
    private static String[] colNames;
    private static String[] colTypes;

    static {
        colNames = new String[]{"flowid"};
        colTypes = new String[]{"BINARY(22)"};
    }
    
    private EntryBinding dataBinding;
    private EntryBinding indexKeyBinding;
    
    private DatabaseEntry flow1key;
    private DatabaseEntry flow2key;
    private NetworkChannelFlowID tmpKey;
    
    public ChannelFlowIDKeyCreator() {
    }

    public void createSecondaryKeys(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, Set results) throws DatabaseException {
        tmpKey = new NetworkChannelFlowID(); // Maybe can be reused?
        flow1key = new DatabaseEntry();
        flow2key = new DatabaseEntry();

        Object entry = null;
        
        entry = dataBinding.entryToObject(data);
        
        if(entry instanceof NetworkChannel) {
            NetworkChannel channel = (NetworkChannel)entry;
            NetworkChannelFlowID k = channel.getChannelFlowID();
            
            tmpKey.setKey(k.getSourceEntity(), k.getDestinationEntity(), k.getSourcePort(), k.getDestinationPort(), k.getProtocol());
            indexKeyBinding.objectToEntry(tmpKey, flow1key);
            
            tmpKey.setKey(k.getDestinationEntity(), k.getSourceEntity(), k.getDestinationPort(), k.getSourcePort(), k.getProtocol());
            indexKeyBinding.objectToEntry(tmpKey, flow2key);
            
            results.add(flow1key);
            results.add(flow2key);
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

    private byte[] buff;
    public void getSqlFieldValue(EnvironmentEntry entry, PreparedStatement s, int f) throws Exception {
        if(entry instanceof NetworkChannel) {
            NetworkChannel channel = (NetworkChannel)entry;
            NetworkChannelFlowID id = channel.getChannelFlowID();
            
            if(buff == null) {
                buff = new byte[id.getByteSize()];
            }
            
            id.serialize(buff, 0);
            s.setBytes(f, buff);
            
            return;
        }
        
        throw new RuntimeException("Expected: " + NetworkChannel.class);
    }

    public void getSqlFieldValue(Object key, PreparedStatement s, int f) throws Exception {
        if(key instanceof NetworkChannelFlowID) {
            NetworkChannelFlowID k = (NetworkChannelFlowID)key;
            
            byte[] b = new byte[k.getByteSize()];
            k.serialize(b, 0);
            s.setBytes(f, b);
            
            return;
        }
        
        throw new RuntimeException("Expected: " + NetworkChannelFlowID.class);
    }
}