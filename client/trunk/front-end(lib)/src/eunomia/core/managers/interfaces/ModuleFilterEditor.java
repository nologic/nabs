/*
 * FilterEditor.java
 *
 * Created on November 6, 2006, 8:20 PM
 *
 */

package eunomia.core.managers.interfaces;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ModuleFilterEditor {
    public void editModuleFilterResp(FrontendProcessorModule module, Filter f);
}
