/*
 * FlowProducerModule.java
 *
 * Created on September 15, 2006, 10:53 PM
 */

package eunomia.module;

import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.flow.Flow;
import eunomia.messages.FilterEntryMessage;
import com.vivic.eunomia.module.frontend.FilterEntryEditor;
import com.vivic.eunomia.module.receptor.FlowCreator;
import com.vivic.eunomia.module.flow.FlowModule;

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

    public Class[] getFilterMessageClassList() {
        return module.getFilterMessageClassList();
    }
}