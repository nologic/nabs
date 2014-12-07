package dbcycletest.db.creators;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.bind.tuple.TupleTupleKeyCreator;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDEndTimeKeyCreator extends TupleTupleKeyCreator {
    private long endTimeInterval;
    
    @Override
    public boolean createSecondaryKey(TupleInput keyInput, TupleInput dataInput, TupleOutput keyOutput) {
        dataInput.skipFast(21);
        endTimeInterval = dataInput.readUnsignedInt();
        endTimeInterval >>= 10;
        keyOutput.writeUnsignedInt(endTimeInterval);
        return true;
    }
}
