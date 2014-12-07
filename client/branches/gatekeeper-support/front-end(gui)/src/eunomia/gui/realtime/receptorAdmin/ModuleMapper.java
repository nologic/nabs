/*
 * ModuleMapper.java
 *
 * Created on March 23, 2006, 10:13 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.managers.ModuleDescriptor;
import eunomia.core.managers.event.linker.MissingDependencyEvent;
import eunomia.core.managers.event.linker.ModuleFileAddedEvent;
import eunomia.core.managers.event.linker.ModuleFileRemovedEvent;
import eunomia.core.managers.listeners.ModuleLinkerListener;
import eunomia.core.receptor.Receptor;
import eunomia.gui.FileChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleMapper extends JPanel implements ModuleLinkerListener, ListSelectionListener {
    private static Logger logger = Logger.getLogger(ModuleMapper.class);
    
    private JList list;
    private JTextArea longDesc;
    private Receptor receptor;
    
    public ModuleMapper(Receptor rec) {
        receptor = rec;
        addControls();
        
        listChanged();
        rec.getLinker().addModuleLinkerListener(this);
    }
    
    public void missingDependency(MissingDependencyEvent e) {
    }

    public void moduleFileAdded(ModuleFileAddedEvent e) {
        listChanged();
    }

    public void moduleFileRemoved(ModuleFileRemovedEvent e) {
        listChanged();
    }

    private void listChanged() {
        Object o = list.getSelectedValue();
        list.setListData(receptor.getLinker().getDescriptors().toArray());
        list.setSelectedValue(o, true);
    }
        
    public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()){
            ModuleDescriptor name = (ModuleDescriptor)list.getSelectedValue();
            if(name != null){
                longDesc.setText(name.getDescriptor().longDescription());
            }
        }
    }
    
    private void addControls() {
        JPanel leftComp = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        leftComp.add(new JScrollPane(list = new JList()));
        
        add(leftComp);
        add(new JScrollPane(longDesc = new JTextArea(3, 30)), BorderLayout.NORTH);
        
        list.addListSelectionListener(this);
        list.setCellRenderer(new MDRenderer());
        longDesc.setWrapStyleWord(true);
    }
    
    private class MDRenderer implements ListCellRenderer {
        private DefaultListCellRenderer label;
        
        public MDRenderer(){
            label = new DefaultListCellRenderer();
            label.setPreferredSize(new Dimension(200, 50));
            label.setFont(new Font("Monospaced", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEtchedBorder());
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Color background;
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            if(value instanceof ModuleDescriptor){
                StringBuilder builder = new StringBuilder();
                ModuleDescriptor md = (ModuleDescriptor)value;
                int instances = receptor.getManager().getModuleHandles(md.getName(), md.getType()).size();
                
                builder.append("<html><body>");
                builder.append("<table border=0 width=100%><tr>");
                builder.append("<td><b>Name:</b> " + md.getName() + "</td>");
                builder.append("<td><b>Type:</b> " + ModuleDescriptor.types[md.getType()] + "</td>");
                builder.append("<td><b>Instances:</b> " + instances + "</td>");
                builder.append("</tr></table>");
                builder.append("<b>Description: </b>");
                builder.append(md.getDescriptor().shortDescription());
                builder.append("</body></html>");
                
                label.setText(builder.toString());
            }
            
            return label;
        }
    }
}