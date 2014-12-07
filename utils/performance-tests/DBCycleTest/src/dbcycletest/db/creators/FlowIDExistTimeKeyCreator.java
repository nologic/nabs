package dbcycletest.db.creators;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Set;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDExistTimeKeyCreator extends TupleTupleMultiKeyCreator {
    private long startTimeInterval;
    private long endTimeInterval;

    @Override
    public void createSecondaryKeys(TupleInput primaryKeyInput, TupleInput dataInput, Set<TupleOutput> indexKeys) {
        TupleOutput interval;
        
        dataInput.skipFast(13);
        startTimeInterval = dataInput.readUnsignedInt();
        startTimeInterval >>= 10;
        
        dataInput.skipFast(4);
        endTimeInterval = dataInput.readUnsignedInt();
        endTimeInterval >>= 10;
        
        for (long i = startTimeInterval; i <= endTimeInterval; ++i) {
            interval = getTupleOutput(null);
            interval.writeUnsignedInt(i);
            indexKeys.add(interval);
        }
    }
}
