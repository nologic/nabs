/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.flow.DNSFlow;

import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.module.frontend.FilterEntryEditor;
import java.awt.GridLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author justin
 */
public class DNSFilterEditor extends JPanel implements FilterEntryEditor {
    private JCheckBox[] types;
    
    private DNSFilterEntry dnsEntry;
    
    private DNSFilterEditor() {
        addControls();
    }
    
    private void addControls() {
        setLayout(new GridLayout(4, 4));
        
        types = new JCheckBox[DNSFlow.NUM_TYPES];
        for (int i = 0; i < types.length; i++) {
            add(types[i] = new JCheckBox(DNSFlow.typeNames[i]));
        }
    }
    
    public void setFilterEntry(FilterEntry entry) {
        if (entry instanceof DNSFilterEntry) {
            setEnabled(true);
            
            dnsEntry = (DNSFilterEntry) entry;
            for (int i = 0; i < types.length; i++) {
                types[i].setSelected(dnsEntry.isAllowedType(i));
            }
        } else {
            setEnabled(false);
        }
    }

    public JComponent getComponent() {
        return this;
    }

    public void commitChanges() {
        for (int i = 0; i < types.length; i++) {
            dnsEntry.setAllowedType(i, types[i].isSelected());
        }
    }
}
