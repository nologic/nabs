/*
 * FlowProducerModule.java
 *
 * Created on September 15, 2006, 10:53 PM
 */

package eunomia.modules;

import eunomia.flow.FilterEntry;
import eunomia.flow.Flow;
import eunomia.messages.FilterEntryMessage;
import eunomia.receptor.module.interfaces.FilterEntryEditor;
import eunomia.receptor.module.interfaces.FlowCreator;
import eunomia.receptor.module.interfaces.FlowModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FlowProducerModule implements FlowModule {
    private FlowModule module;
    
    public FlowProducerModule(FlowModule mod) {
        module = mod;
    }

    public FlowModule getModule() {
        return module;
    }

    public void setModule(FlowModule module) {
        this.module = module;
    }
    
    public String getFlowClass() {
        return module.getFlowClass();
    }

    public String getCreatorClass() {
        return module.getCreatorClass();
    }

    public Flow getNewFlowInstance() {
        return module.getNewFlowInstance();
    }

    public FlowCreator getNewFlowCreatorInstance() {
        return module.getNewFlowCreatorInstance();
    }

    public FilterEntry getNewFilterEntry(FilterEntryMessage fem) {
        return module.getNewFilterEntry(fem);
    }

    public FilterEntryEditor getFilterEditor() {
        return module.getFilterEditor();
    }
}