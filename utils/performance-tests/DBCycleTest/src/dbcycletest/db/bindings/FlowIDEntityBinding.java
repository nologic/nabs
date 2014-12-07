package dbcycletest.db.bindings;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.bind.tuple.TupleTupleBinding;
import dbcycletest.FlowID;
import java.io.IOException;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDEntityBinding extends TupleTupleBinding {
    private byte[] padding;
    
    public FlowIDEntityBinding() {
        padding = new byte[171];
        for (int i = 0; i < 171; ++i) {
            padding[i] = 0;
        }
    }

    @Override
    public Object entryToObject(TupleInput key, TupleInput data) {

        FlowID id = new FlowID();

        id.setKey(key.readInt());

        id.setProtocol(data.readByte());
        id.setSourceIP(data.readUnsignedInt());
        id.setDestinationIP(data.readUnsignedInt());
        id.setSourcePort(data.readUnsignedShort());
        id.setDestinationPort(data.readUnsignedShort());
        id.setStartTimeSeconds(data.readUnsignedInt());
        id.setStartTimeMicroseconds(data.readUnsignedInt());
        id.setEndTimeSeconds(data.readUnsignedInt());
        id.setEndTimeMicroseconds(data.readUnsignedInt());

        return id;
    }

    @Override
    public void objectToKey(Object obj, TupleOutput key) {
        FlowID id = (FlowID) obj;
        
        key.writeInt(id.getKey());
    }

    @Override
    public void objectToData(Object obj, TupleOutput data) {
        try {
            FlowID id = (FlowID) obj;

            data.writeByte(id.getProtocol() & 0x000000FF);
            data.writeUnsignedInt(id.getSourceIP());
            data.writeUnsignedInt(id.getDestinationIP());
            data.writeUnsignedShort(id.getSourcePort());
            data.writeUnsignedShort(id.getDestinationPort());
            data.writeUnsignedInt(id.getStartTimeSeconds());
            data.writeUnsignedInt(id.getStartTimeMicroseconds());
            data.writeUnsignedInt(id.getEndTimeSeconds());
            data.writeUnsignedInt(id.getEndTimeMicroseconds());
            
            data.write(padding);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}