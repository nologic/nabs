/*
 * FilterEntryEditor.java
 *
 * Created on July 4, 2006, 8:07 PM
 *
 */

package com.vivic.eunomia.module.receptor;

import eunomia.flow.FilterEntry;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface FilterEntryEditor {
    public void setFilterEntry(FilterEntry entry);
    public JComponent getComponent();
    public void commitChanges();
}
