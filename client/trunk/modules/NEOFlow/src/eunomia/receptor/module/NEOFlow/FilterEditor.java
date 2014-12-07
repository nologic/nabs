/*
 * FilterEditor.java
 *
 * Created on July 4, 2006, 7:59 PM
 *
 */

package eunomia.receptor.module.NEOFlow;

import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.frontend.FilterEntryEditor;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * GUI compoment used for editing the flow type allowed by the filter. The type
 * is a NABFlow module specific detail.
 * @author Mikhail Sosonkin
 */
public class FilterEditor extends JPanel implements FilterEntryEditor {
    /**
     * Check boxes for selecting the types. Array index corresponds to the flow type.
     */
    private JCheckBox[] types;
    
    /**
     * Entry to be edited.
     */
    private NEOFilterEntry nabEntry;
    
    public FilterEditor() {
        // The GUI component is the this object. This is not neccessary.
        addControls();
    }
    
    /**
     * Initialize the GUI components.
     */
    private void addControls(){
        setLayout(new GridLayout(4, 4));
        
        // Each type has it own box.
        types = new JCheckBox[NEOFlow.NUM_TYPES];
        for (int i = 0; i < types.length; i++) {
            add(types[i] = new JCheckBox(NEOFlow.typeNames[i]));
        }
    }

    /**
     * When editing begins the front-end will call this method to tell the editor which
     * entry to edit. It is up to the editor to extract the relevant information.
     * @param entry Not guaranteed to be of the correct type, so it needs to be checked to ensure
     * this module can support the entry type.
     */
    public void setFilterEntry(FilterEntry entry) {
        // This module supports only one type of entry, the check is required. Generally
        // the front-end will have logic to passing correct types, however the check should
        // still be performed.
        if(entry instanceof NEOFilterEntry){
            setEnabled(true);
            
            // The entry has the list of selected types. Transfer that to the check boxes.
            nabEntry = (NEOFilterEntry)entry;
            for (int i = 0; i < types.length; i++) {
                types[i].setSelected(nabEntry.isAllowedType(i));
            }
        } else {
            // unsuppoerted types should not be edited here.
            setEnabled(false);
        }
    }

    /**
     * This method will be called after <CODE>setFilterEntry()</code> is called.
     * @return GUI component for displaying to the user.
     */
    public JComponent getComponent() {
        return this;
    }

    /**
     * This method will be executed buy the front-end to indicate when the user has
     * finished editing the entry. At this point the information from the GUI should
     * be transfered to the entry.
     */
    public void commitChanges() {
        // set the user selections. It is recommened to not do this on the fly because editing
        // can be canceled by the user. In that case we want the entry to remain unchanged.
        for (int i = 0; i < types.length; i++) {
            nabEntry.setAllowedType(i, types[i].isSelected());
        }
    }
}