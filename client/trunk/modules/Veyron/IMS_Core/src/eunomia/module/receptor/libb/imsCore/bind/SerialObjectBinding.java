/*
 * SerialObjectBinding.java
 *
 * Created on January 27, 2008, 11:38 AM
 *
 */

package eunomia.module.receptor.libb.imsCore.bind;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.db.DatabaseEntry;
import com.vivic.eunomia.sys.util.Util;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SerialObjectBinding implements EntryBinding {
    // we assume that this will handle only one class type!
    private BoundObject sample;
    private byte[] buff;
    
    public SerialObjectBinding(BoundObject sample) {
        this.sample = sample;
    }

    public Object entryToObject(DatabaseEntry entry) {
        BoundObject o = sample.clone();
        o.unserialize(entry.getData(), 0);

        return o;
    }

    public void objectToEntry(Object object, DatabaseEntry entry) {
        BoundObject bobj = (BoundObject)object;
        int size = bobj.getByteSize();
        
        byte[] array = new byte[size];

        bobj.serialize(array, 0);
        entry.setData(array, 0, size);
    }
    
    public byte[] getNewBuffer(BoundObject o) {
        buff = new byte[o.getByteSize()];
        o.serialize(buff, 0);
        
        return buff;
    }
    
    public byte[] getBuffer(BoundObject o) {
        if(buff == null) {
            buff = new byte[o.getByteSize()];
        }
        
        o.serialize(buff, 0);
        
        return buff;
    }
    
    public BoundObject getObject(byte[] b) {
        BoundObject o = sample.clone();
        o.unserialize(b, 0);
        
        return o;
    }
    
    public String toString() {
        return "Binding: " + sample.getClass();
    }
}