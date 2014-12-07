/*
 * AnlzComponent.java
 *
 * Created on February 11, 2008, 8:54 PM
 *
 */

package eunomia.module.receptor.proc.exProcAnlz;

import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import eunomia.module.receptor.libb.imsCore.net.Network;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnlzComponent implements VeyronAnalysisComponent {
    private Network net;
    
    public AnlzComponent() {
    }

    public void initialize(Network net) {
        this.net = net;
    }

    public void executeAnalysis() {
        // Do the work! analyze stuff here.
    }
    
}
