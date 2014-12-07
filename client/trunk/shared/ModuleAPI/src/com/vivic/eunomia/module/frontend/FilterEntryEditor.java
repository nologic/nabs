/*
 * FilterEntryEditor.java
 *
 * Created on July 4, 2006, 8:07 PM
 *
 */

package com.vivic.eunomia.module.frontend;

import com.vivic.eunomia.filter.FilterEntry;
import javax.swing.JComponent;

/**
 * Used by analysis console to edit flow specific information of
 * the module.
 * @author Mikhail Sosonkin
 */
public interface FilterEntryEditor {
    /**
     * Defines which entry to set for modifications
     * @param entry Filter entry to modify
     */
    public void setFilterEntry(FilterEntry entry);
    /**
     * Returns the component for editing Specific data.
     * 
     * This component should provide UI for modifying data not accessible though the
     * FilterEntry class. Changing other components will be provided by the Console.
     * @return Swing compatible interface
     */
    public JComponent getComponent();
    /**
     * The changes should not be commited until this function is called. Generally this
     * event would be triggered by the user.
     */
    public void commitChanges();
}
