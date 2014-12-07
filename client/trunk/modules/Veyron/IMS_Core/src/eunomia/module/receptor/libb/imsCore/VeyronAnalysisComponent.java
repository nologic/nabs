/*
 * VeyronAnalysisModule.java
 *
 * Created on January 24, 2008, 9:10 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface VeyronAnalysisComponent {
    public void initialize(NetworkTopology net, NetworkSymbols syms);
    public void setReporter(Reporter report);
    public void executeAnalysis();
}