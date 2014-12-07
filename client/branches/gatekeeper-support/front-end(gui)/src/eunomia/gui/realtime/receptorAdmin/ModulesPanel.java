/*
 * ModulesPanel.java
 *
 * Created on January 1, 2006, 11:41 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.receptor.Receptor;
import eunomia.gui.realtime.receptorAdmin.ModuleMapper;
import java.awt.BorderLayout;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModulesPanel extends JPanel {
    private Receptor receptor;
    private ModuleMapper mapper;
    
    public ModulesPanel(Receptor receptor) {
        this.receptor = receptor;
        
        this.setLayout(new BorderLayout());
        this.add(mapper = new ModuleMapper(receptor));
    }
}