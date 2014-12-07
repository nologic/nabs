/*
 * Main.java
 *
 * Created on August 23, 2006, 8:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.receptor.module.netFlow;

import eunomia.flow.FilterEntry;
import eunomia.flow.Flow;
import eunomia.receptor.module.interfaces.FilterEntryEditor;
import eunomia.receptor.module.interfaces.FlowCreator;
import eunomia.receptor.module.interfaces.FlowModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements FlowModule {
    private NetFlowCreator creator;
    
    public Main() {
        creator = new NetFlowCreator();
    }

    public String getFlowClass() {
        return "eunomia.receptor.module.netFlow.NetFlow";
    }

    public String getCreatorClass() {
        return "eunomia.receptor.module.netFlow.NetFlowCreator";
    }

    public Flow getNewFlowInstance() {
        return new NetFlow();
    }

    public FlowCreator getNewFlowCreatorInstance() {
        return creator;
    }

    public FilterEntry getNewFilterEntry(eunomia.messages.FilterEntryMessage fem) {
        return null;
    }

    public FilterEntryEditor getFilterEditor() {
        return null;
    }
}
