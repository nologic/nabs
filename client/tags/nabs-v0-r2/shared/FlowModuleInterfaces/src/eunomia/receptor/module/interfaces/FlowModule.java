/*
 * FlowModule.java
 *
 * Created on June 19, 2006, 10:05 PM
 *
 */

package eunomia.receptor.module.interfaces;

import eunomia.flow.FilterEntry;
import eunomia.flow.Flow;
import eunomia.messages.FilterEntryMessage;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface FlowModule {
    public String getFlowClass();
    public String getCreatorClass();
    public Flow getNewFlowInstance();
    public FlowCreator getNewFlowCreatorInstance();
    public FilterEntry getNewFilterEntry(FilterEntryMessage fem);
    public FilterEntryEditor getFilterEditor();
}