/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB.creators;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Set;

/**
 *
 * @author justin
 */
public class NeoflowExistTimeKeyCreator extends TupleTupleMultiKeyCreator {

    @Override
    public void createSecondaryKeys(TupleInput primaryKeyInput,
                                    TupleInput dataInput,
                                    Set<TupleOutput> indexKeys) {
        
        long begin, end;
        
        primaryKeyInput.skipFast(13);
        begin = primaryKeyInput.readUnsignedInt() >> 10;
        primaryKeyInput.skipFast(4);
        end = primaryKeyInput.readUnsignedInt() >> 10;
        
        for (long i = begin; i <= end; ++i) {
            TupleOutput o = getTupleOutput(null);
            
            o.writeUnsignedInt(i);
            indexKeys.add(o);
        }
    }
}
