/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.NeoDB.bindings;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 *
 * @author justin
 */
public class UnsignedIntegerBinding extends TupleBinding {

    @Override
    public Object entryToObject(TupleInput input) {
        return new Long(input.readUnsignedInt());
    }

    @Override
    public void objectToEntry(Object o, TupleOutput output) {
        Long l = (Long) o;
        output.writeUnsignedInt(l.longValue());
    }

}
