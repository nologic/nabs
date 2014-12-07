/*
 * ModuleMapper.java
 *
 * Created on March 23, 2006, 10:13 PM
 *
 */

package eunomia.gui.settings;

import eunomia.core.managers.*;
import eunomia.core.managers.listeners.*;
import eunomia.gui.FileChooser;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ModuleMapper extends JPanel implements ActionListener, ModuleLinkerListener,
        ListSelectionListener {
    private static Logger logger = Logger.getLogger(ModuleMapper.class);
    private JList list;
    private JTextArea longDesc;
    private JButton add, remove;
    
    public ModuleMapper() {
        addControls();
        
        listChanged();
        ModuleLinker.v().addModuleLinkerListener(this);
    }
    
    public void listChanged() {
        Object o = list.getSelectedValue();
        list.setListData(ModuleLinker.v().getDescriptors().toArray());
        list.setSelectedValue(o, true);
    }
    
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == add){
            addNew();
        } else if(o == remove){
            removeModule((ModuleDescriptor)list.getSelectedValue());
        }
    }
    
    private void removeModule(ModuleDescriptor desc){
        if(desc != null){
            ModuleLinker.v().removeMapping(desc);
        }
    }
    
    private void addNew(){
        File sel = FileChooser.getOpenFile();
        if(sel != null){
            ModuleDescriptor md;
            try {
                md = ModuleLinker.v().loadModule(sel.toString());
                listChanged();
                list.setSelectedValue(md, true);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                //ex.printStackTrace();
            }
        }
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
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        
        setLayout(new BorderLayout());
        
        buttonsPanel.add(add = new JButton("Add"));
        buttonsPanel.add(remove = new JButton("Remove"));
        
        leftComp.add(new JScrollPane(list = new JList()));
        leftComp.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(leftComp);
        add(new JScrollPane(longDesc = new JTextArea(3, 30)), BorderLayout.NORTH);
        
        add.addActionListener(this);
        remove.addActionListener(this);
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
                builder.append("<html><body>");
                builder.append("<table border=0 width=100%><tr>");
                builder.append("<td><b>Name:</b> " + md.getName() + "</td>");
                builder.append("<td><b>Type:</b> " + ModuleDescriptor.types[md.getType()] + "</td>");
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