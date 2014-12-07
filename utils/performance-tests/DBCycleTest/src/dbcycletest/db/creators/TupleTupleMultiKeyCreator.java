package dbcycletest.db.creators;

import com.sleepycat.bind.tuple.TupleBase;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryMultiKeyCreator;
import java.util.HashSet;
import java.util.Set;

    
/**
 *
 * @author Justin Stallard
 */
public abstract class TupleTupleMultiKeyCreator extends TupleBase
    implements SecondaryMultiKeyCreator {

    public void createSecondaryKeys(SecondaryDatabase db, 
                                    DatabaseEntry primaryKeyEntry,
                                    DatabaseEntry dataEntry,
                                    Set indexKeySet) 
        throws DatabaseException {
        
        TupleInput primaryKeyInput = entryToInput(primaryKeyEntry);
        TupleInput dataInput = entryToInput(dataEntry);
        Set<TupleOutput> indexOutputSet = new HashSet<TupleOutput>();
        createSecondaryKeys(primaryKeyInput, dataInput, indexOutputSet);

        for (TupleOutput o : indexOutputSet) {
            DatabaseEntry e = new DatabaseEntry();
            
            if (o.getBufferLength() == 2) {
                System.err.println("TupleTupleMulti: tuple length: " + o.getBufferLength());
            }
            
            outputToEntry(o, e);
            
            if (e.getSize() == 2) {
                System.err.println("TupleTupleMulti: entry length: " + e.getSize());
            }
            
            indexKeySet.add(e);
        }
        
        return;
    }

    public abstract void createSecondaryKeys(TupleInput primaryKeyInput,
                                             TupleInput dataInput,
                                             Set<TupleOutput> indexKeys);
}
