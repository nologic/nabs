/*
 * FlowModule.java
 *
 * Created on June 19, 2006, 10:05 PM
 *
 */

package com.vivic.eunomia.module.receptor;

import com.vivic.eunomia.module.EunomiaModule;
import eunomia.flow.FilterEntry;
import com.vivic.eunomia.module.Flow;
import eunomia.messages.FilterEntryMessage;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface FlowModule extends EunomiaModule {
    public Flow getNewFlowInstance();
    public FlowCreator getNewFlowCreatorInstance();
    public FilterEntry getNewFilterEntry(FilterEntryMessage fem);
    public FilterEntryEditor getFilterEditor();
    public Class[] getFilterMessageClassList();
}