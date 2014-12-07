package dbcycletest.db.bindings;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import dbcycletest.db.keys.FlowIDFlowIDKey;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDFlowIDKeyBinding extends TupleBinding {

    @Override
    public Object entryToObject(TupleInput key) {
        try {
            //System.err.println("entryToObect called...");
            if (key.getBufferLength() != 13) {
                System.err.println("ERROR!!!!!!! buffer has " + key.getBufferLength() + " bytes!!");
            }
            FlowIDFlowIDKey flowIDFlowIDKey = new FlowIDFlowIDKey();

            flowIDFlowIDKey.setProtocol(key.readByte());
            flowIDFlowIDKey.setSourceIP(key.readUnsignedInt());
            flowIDFlowIDKey.setDestinationIP(key.readUnsignedInt());
            flowIDFlowIDKey.setSourcePort(key.readUnsignedShort());
            flowIDFlowIDKey.setDestinationPort(key.readUnsignedShort());

            return (Object) flowIDFlowIDKey;
        } catch (Exception e) {
            System.out.println("key input length: " + key.getBufferLength());
            key.reset();
            for (int i = 0; i < key.getBufferLength(); ++i) {
                System.out.println((int) (key.readByte() & 0xFF));
            }
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void objectToEntry(Object objKey, TupleOutput key) {
        FlowIDFlowIDKey flowIDFlowIDKey = (FlowIDFlowIDKey) objKey;
        key.writeByte(flowIDFlowIDKey.getProtocol());
        key.writeUnsignedInt(flowIDFlowIDKey.getSourceIP());
        key.writeUnsignedInt(flowIDFlowIDKey.getDestinationIP());
        key.writeUnsignedShort(flowIDFlowIDKey.getSourcePort());
        key.writeUnsignedShort(flowIDFlowIDKey.getDestinationPort());
    }

}
