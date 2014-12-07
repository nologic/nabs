package dbcycletest.db.bindings;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import dbcycletest.db.keys.FlowIDHostKey;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDHostKeyBinding extends TupleBinding {

    @Override
    public Object entryToObject(TupleInput key) {
        if (key.getBufferLength() != 4) {
            System.err.println("ERROR!!!!!!! buffer has " + key.getBufferLength() + " bytes!!");
        }
        FlowIDHostKey flowIDHostKey = new FlowIDHostKey();
        
        flowIDHostKey.setIP(key.readUnsignedInt());
        
        return (Object) flowIDHostKey;
    }

    @Override
    public void objectToEntry(Object objKey, TupleOutput key) {
        FlowIDHostKey flowIDHostKey = (FlowIDHostKey) objKey;
        
        key.writeUnsignedInt(flowIDHostKey.getIP());
    }

}
