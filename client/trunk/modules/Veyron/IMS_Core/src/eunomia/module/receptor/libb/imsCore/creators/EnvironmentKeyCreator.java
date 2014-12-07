/*
 * AbstractEnvironmentKeyCreator.java
 *
 * Created on March 8, 2008, 8:42 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import com.sleepycat.bind.EntryBinding;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface EnvironmentKeyCreator {
    public void setDataBinding(EntryBinding dataBinding);
    public void setIndexKeyBinding(EntryBinding indexKeyBinding);
}