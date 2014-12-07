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
public class NeoflowHostKeyCreator extends TupleTupleMultiKeyCreator {

    @Override
    public void createSecondaryKeys(TupleInput primaryKeyInput, TupleInput dataInput, Set<TupleOutput> indexKeys) {
        TupleOutput src = getTupleOutput(null);
        TupleOutput dest = getTupleOutput(null);
        
        primaryKeyInput.skipFast(5);
        src.writeUnsignedInt(primaryKeyInput.readUnsignedInt());
        dest.writeUnsignedInt(primaryKeyInput.readUnsignedInt());
        
        indexKeys.add(src);
        indexKeys.add(dest);
    }
}
