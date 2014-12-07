package dbcycletest.db.creators;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDHostKeyCreator extends TupleTupleMultiKeyCreator {
    private byte[] ip;
    
    public FlowIDHostKeyCreator() {
        ip = new byte[4];
    }

    @Override
    public void createSecondaryKeys(TupleInput primaryKeyInput, TupleInput dataInput, Set<TupleOutput> indexKeys) {
        try {
            TupleOutput src = getTupleOutput(null);
            TupleOutput dest = getTupleOutput(null);

            dataInput.skipFast(1);
            dataInput.read(ip);
            src.write(ip);
            dataInput.read(ip);
            dest.write(ip);
            
            indexKeys.add(src);
            indexKeys.add(dest);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
