package dbcycletest.db.creators;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDFlowIDKeyCreator extends TupleTupleMultiKeyCreator {
    
    public FlowIDFlowIDKeyCreator() {
    }
    
    @Override
    public void createSecondaryKeys(TupleInput primaryKeyInput, TupleInput dataInput, Set<TupleOutput> indexKeys) {
        try {
            byte[] flowID = new byte[13];
            TupleOutput id = getTupleOutput(null);
            TupleOutput reverseID = getTupleOutput(null);
            
            if (dataInput.read(flowID) != 13) {
                System.err.println("WTF!?!?!?!?!??!?");
            }
            id.write(flowID, 0, 13);
            reverseID.write(flowID, 0, 1);
            reverseID.write(flowID, 5, 4);
            reverseID.write(flowID, 1, 4);
            reverseID.write(flowID, 11, 2);
            reverseID.write(flowID, 9, 2);
            
            if (id.getBufferLength() != 13) {
                System.err.println("WTF??????? didn't write 13 bytes!!!!!!");
            }
            indexKeys.add(id);
            if (reverseID.getBufferLength() != 13) {
                System.err.println("WTF??????? didn't write 13 bytes for reverse!!!");
            }
            indexKeys.add(reverseID);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
