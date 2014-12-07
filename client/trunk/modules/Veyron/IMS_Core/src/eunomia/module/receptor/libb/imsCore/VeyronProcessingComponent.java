/*
 * VeyronModule.java
 *
 * Created on January 13, 2008, 3:28 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface VeyronProcessingComponent {
    public void initialize(NetworkTopology net, NetworkSymbols syms);
    public void setReporter(Reporter report);
}