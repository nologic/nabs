/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB.creators;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.bind.tuple.TupleTupleKeyCreator;

/**
 *
 * @author justin
 */
public class NeoflowEndTimeKeyCreator extends TupleTupleKeyCreator {

    @Override
    public boolean createSecondaryKey(TupleInput primaryKeyInput,
                                      TupleInput dataInput,
                                      TupleOutput keyOutput) {
        
        primaryKeyInput.skipFast(21);
        keyOutput.writeUnsignedInt(primaryKeyInput.readUnsignedInt() >> 10);
        
        return true;
    }

}
