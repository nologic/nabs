/*
 * Main.java
 *
 * Created on June 19, 2006, 10:08 PM
 *
 */

package eunomia.receptor.module.NABFlow;

import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.flow.Flow;
import eunomia.messages.FilterEntryMessage;
import com.vivic.eunomia.module.frontend.FilterEntryEditor;
import com.vivic.eunomia.module.receptor.FlowCreator;
import com.vivic.eunomia.module.flow.FlowModule;
import eunomia.receptor.module.NABFlow.messages.NABFlowSpecificMessage;

/**
 * This is the class used to instantiate this module. The class name is derived
 * using the Descriptor.moduleName() method with the following psudo-code:
 * <CODE>
 *    String moduleClass = "eunomia.receptor.module." + Descriptor.moduleName() +
 *                         ".Main";
 * </CODE>
 * @author Mikhail Sosonkin
 */
public class Main implements FlowModule {
    /**
     * GUI filter editor that will be used by the user to change module specific filter
     * settings. This module maintains a single instance of it.
     */
    private FilterEntryEditor filterEditor;
    
    public Main() {
        filterEditor = new FilterEditor();
    }

    /**
     * This is the reason why <CODE>getFlowClass()</CODE> method is now deprecated,
     * this method will create the new flow Object.
     * @return a new NAB Flow instance.
     */
    public Flow getNewFlowInstance() {
        return new NABFlow();
    }

    /**
     * 
     * @return a new flow creator instance. In general it is possible to return the same object
     * everytime. At the moment the object is used for only one purpose and within the
     * same thread.
     */
    public FlowCreator getNewFlowCreatorInstance() {
        // There are only a few of these created, so it's not a problem to create a new
        // instance everytime.
        return new NABStream();
    }

    /**
     * 
     * @param fem FilterEntryMessage is a message that contains information about the filter
     * entry, it also has the data for module specific portion.
     * @return An entry for this message. The most specific class must the one from the module.
     */
    public FilterEntry getNewFilterEntry(FilterEntryMessage fem) {
        // Used to communicate filters between middleware and the front-end, only the module
        // knows all the inforamtion.
        return new NABFilterEntry(fem);
    }

    /**
     * 
     * @return the filterEditor object. As stated previosly the object can be reused.
     */
    public FilterEntryEditor getFilterEditor() {
        return filterEditor;
    }

    public Class[] getFilterMessageClassList() {
        return new Class[] {
            NABFlowSpecificMessage.class
        };
    }
}