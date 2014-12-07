/*
 * FilterEditor.java
 *
 * Created on November 6, 2006, 8:20 PM
 *
 */

package eunomia.core.managers.interfaces;

import eunomia.flow.Filter;
import com.vivic.eunomia.module.frontend.GUIModule;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ModuleFilterEditor {
    public void editModuleFilterResp(GUIModule module, Filter f);
}
