/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB.creators;

import com.sleepycat.bind.tuple.TupleBase;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryMultiKeyCreator;
import java.util.HashSet;
import java.util.Set;

    
/**
 *
 * @author justin
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
            
            outputToEntry(o, e);
            indexKeySet.add(e);
        }
        
        return;
    }

    public abstract void createSecondaryKeys(TupleInput primaryKeyInput,
                                             TupleInput dataInput,
                                             Set<TupleOutput> indexKeys);
}
