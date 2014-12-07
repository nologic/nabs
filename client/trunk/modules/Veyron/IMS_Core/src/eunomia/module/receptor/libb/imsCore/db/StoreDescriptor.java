/*
 * StoreDescriptor.java
 *
 * Created on March 8, 2008, 7:03 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

import com.sleepycat.bind.EntryBinding;
import eunomia.module.receptor.libb.imsCore.creators.EnvironmentKeyCreator;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StoreDescriptor {
    private String name;
    private EntryBinding keyBinding;
    private EntryBinding valueBinding;
    private EnvironmentKeyCreator keyCreator;
    private boolean modifiable;
    
    //bdb
    public StoreDescriptor(String name, EnvironmentKeyCreator keyCreator, EntryBinding keyBinding, EntryBinding valueBinding, boolean modifiable) {
        this.name = name;
        this.keyCreator = keyCreator;
        this.keyBinding = keyBinding;
        this.valueBinding = valueBinding;
        this.modifiable = modifiable;
        
        if(keyCreator != null) {
            keyCreator.setDataBinding(valueBinding);
            keyCreator.setIndexKeyBinding(keyBinding);
        }
    }
    
    //sql
    public StoreDescriptor(EnvironmentKeyCreator keyCreator, EntryBinding valueBinding) {
        this.keyCreator = keyCreator;
        this.valueBinding = valueBinding;
    }

    public String getName() {
        return name;
    }

    public EntryBinding getKeyBinding() {
        return keyBinding;
    }

    public EntryBinding getValueBinding() {
        return valueBinding;
    }

    public Object getKeyCreator() {
        return keyCreator;
    }

    public boolean isModifiable() {
        return modifiable;
    }
}