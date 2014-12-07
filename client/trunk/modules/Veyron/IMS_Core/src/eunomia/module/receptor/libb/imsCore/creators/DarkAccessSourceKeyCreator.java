/*
 * DarkAccessSourceKeyCreator.java
 *
 * Created on January 26, 2008, 1:08 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.SecondaryDatabase;
import com.sleepycat.db.SecondaryKeyCreator;
import eunomia.module.receptor.libb.imsCore.net.DarkAccess;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DarkAccessSourceKeyCreator implements SecondaryKeyCreator, EnvironmentKeyCreator {
    private EntryBinding dataBinding;
    private LongBinding indexKeyBinding;

    public DarkAccessSourceKeyCreator() {
    }

    public boolean createSecondaryKey(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) throws DatabaseException {
        Object entry = dataBinding.entryToObject(data);
        
        if(entry instanceof DarkAccess) {
            DarkAccess da = (DarkAccess)entry;
            
            // Need to avoid creating this. Make more efficient.
            indexKeyBinding.longToEntry(da.getFlowID().getSourceEntity().getIPv4(), result);
            
            return true;
        }
        
        return false;
    }

    public void setDataBinding(EntryBinding dataBinding) {
        this.dataBinding = dataBinding;
    }

    public void setIndexKeyBinding(EntryBinding indexKeyBinding) {
        this.indexKeyBinding = (LongBinding) indexKeyBinding;
    }
}