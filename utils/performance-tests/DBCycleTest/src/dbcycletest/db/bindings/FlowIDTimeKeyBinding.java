package dbcycletest.db.bindings;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import dbcycletest.db.keys.FlowIDTimeKey;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDTimeKeyBinding extends TupleBinding {

    @Override
    public Object entryToObject(TupleInput key) {
        if (key.getBufferLength() != 4) {
            System.err.println("ERROR!!!!!!! buffer has " + key.getBufferLength() + " bytes!!");
            System.err.println(key.readByte() + " " + key.readByte());
        }
        FlowIDTimeKey flowIDEndTimeKey = new FlowIDTimeKey();
        
        flowIDEndTimeKey.setEndTimeInterval(key.readUnsignedInt());
        
        return (Object) flowIDEndTimeKey;
    }

    @Override
    public void objectToEntry(Object objKey, TupleOutput key) {
        FlowIDTimeKey flowIDEndTimeKey = (FlowIDTimeKey) objKey;
        
        key.writeUnsignedInt(flowIDEndTimeKey.getEndTimeInterval());
    }

}
