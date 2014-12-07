/*
 * ComponentRegistryListener.java
 *
 * Created on September 11, 2008, 11:22 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.util;

import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import eunomia.module.receptor.libb.imsCore.util.ComponentRegistry.AnlComp;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ComponentRegistryListener {
    public void processingComponentAdded(VeyronProcessingComponent comp);
    public void analysisComponentAdded(AnlComp anl);
    public void reporterComponentAdded(Reporter rep);
}