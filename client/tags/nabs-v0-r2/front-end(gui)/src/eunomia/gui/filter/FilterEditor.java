/*
 * FilterEditor.java
 *
 * Created on August 1, 2005, 3:25 PM
 *
 */

package eunomia.gui.filter;
import eunomia.core.managers.FlowModuleManager;
import eunomia.core.managers.interfaces.ModuleFilterEditor;
import eunomia.flow.Filter;
import eunomia.flow.FilterEntry;
import eunomia.flow.FilterList;
import eunomia.plugin.interfaces.GUIModule;
import eunomia.receptor.module.interfaces.FlowModule;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FilterEditor implements ActionListener, WindowListener, ModuleFilterEditor {
    private static FilterEditor ins;
    
    private FilterRenderer renderer;
    private ListPanel bList;
    private ListPanel wList;
    private Filter filter;
    private EntryEditor editor;
    private GUIModule currentModule;
    private JButton commit;
    private JDialog dialog;
    private JPanel contentPane;
    private boolean isCommited;
    
    public FilterEditor(Frame owner) {
        renderer = new FilterRenderer();
        contentPane = new JPanel();
        addControls();
    }
    
    private void setFilter(Filter f){
        isCommited = true;
        filter = f;
        bList.setList(f.getBlackList());
        wList.setList(f.getWhiteList());
    }
    
    private void editModuleFilter(GUIModule module){
        currentModule = module;
        contentPane.setEnabled(false);
        module.getReceptor().getManager().getFilter(module, this);
    }
    
    public void editModuleFilterResp(GUIModule module, Filter f){
        if(currentModule == module){
            contentPane.setEnabled(true);
            setFilter(f);
            dialog.validate();
            dialog.repaint();
            dialog.setTitle("Filter Editor");
        }
    }
    
    public static void initialize(Frame owner){
        if(ins == null){
            ins = new FilterEditor(owner);
        }
    }
    
    public static void editFilter(GUIModule module){
        if(ins != null){
            ins.editModuleFilter(module);
            ins.dialog.setVisible(true);
        }
    }
        
    public static void editFilter(Frame ownerOveride, GUIModule module){
        if(ins != null){
            ins.dialog = new JDialog(ownerOveride, true);
            ins.dialog.setTitle("Filter Editor - Obtaining Filter - Please Wait");
            ins.dialog.setSize(500, 300);
            ins.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ins.dialog.addWindowListener(ins);
            ins.dialog.setContentPane(ins.contentPane);
            ins.dialog.setLocationRelativeTo(ownerOveride);
            ins.editor = new EntryEditor(ins.dialog);
            editFilter(module);
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == commit){
            commitFilter();
        }
    }
    
    private void commitFilter(){
        isCommited = true;
        currentModule.getReceptor().getManager().setFilter(currentModule, filter);
    }
    
    private void addControls(){
        JPanel listsPanel = new JPanel(new GridLayout(2, 1));
        JPanel wListPanel = new JPanel(new BorderLayout());
        JPanel bListPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 1));
        JPanel buttomPanel = new JPanel(new BorderLayout());
        
        Container c = contentPane;
        c.setLayout(new BorderLayout());
        
        wListPanel.add(new JLabel("White List"), BorderLayout.NORTH);
        wListPanel.add(wList = new ListPanel());
        bListPanel.add(new JLabel("Black List"), BorderLayout.NORTH);
        bListPanel.add(bList = new ListPanel());
        listsPanel.add(wListPanel);
        listsPanel.add(bListPanel);
        
        buttonsPanel.add(commit = new JButton("Apply Filter"));
        buttomPanel.add(buttonsPanel, BorderLayout.EAST);
        
        c.add(listsPanel);
        c.add(buttomPanel, BorderLayout.SOUTH);
        
        commit.addActionListener(this);
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if(!isCommited){
            int choice = JOptionPane.showConfirmDialog(dialog, "Do you want to save the changes?", 
                                                        "Change Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if(choice == JOptionPane.NO_OPTION){
                dialog.dispose();
            } else if(choice == JOptionPane.YES_OPTION) {
                commitFilter();
                dialog.dispose();
            }
        } else {
            dialog.dispose();
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    private class ListPanel extends JPanel implements ActionListener {
        private FilterList fList;
        private JList list;
        private JButton moveup;
        private JButton movedown;
        private JButton add;
        private JButton remove;
        private JButton edit;
        private FilterEntry[] entries;
        
        public ListPanel(){
            addControls();
        }
        
        public void setList(FilterList f){
            fList = f;
            updateList();
        }
        
        private void updateList(){
            entries = fList.getAsArray();
            list.setListData(entries);
        }
        
        private void editEntry(){
            FilterEntry entry = (FilterEntry)list.getSelectedValue();
            if(entry != null){
                ins.editor.setEntry(entry);
                
                ins.editor.setLocationRelativeTo(ins.dialog);
                ins.editor.setVisible(true);
                updateList();
            }
        }
        
        private void addEntry(){
            String[] choices = FlowModuleManager.ins.getNamesArray();
            Object selection = null;
            if(choices.length > 1){
                selection = JOptionPane.showInputDialog(this, "Select Flow Type", "Module Selection", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            } else {
                selection = choices[0];
            }
            
            if(selection != null){
                FlowModule fmod = FlowModuleManager.ins.getFlowModuleInstance(selection.toString());
                if(fmod != null){
                    FilterEntry entry = fmod.getNewFilterEntry(null);
                    fList.addEntry(entry);
                    updateList();

                    list.setSelectedValue(entry, true);
                    editEntry();
                }
            }
        }
        
        private void removeEntry(){
            Object[] entry = (Object[])list.getSelectedValues();
            if(entry != null){
                for(int i = 0; i < entry.length; i++){
                    fList.removeEntry((FilterEntry)entry[i]);
                }
                updateList();
            }
        }
        
        /* Assume it is allowed to do the move */
        private void moveEntry(int step, int entry){
            fList.switchEntries(entries[entry], entries[entry + step]);
            updateList();
            list.setSelectedIndex(entry + step);
        }
        
        public void actionPerformed(ActionEvent e){
            isCommited = false;
            Object o = e.getSource();
            if(o == moveup){
                int sel = list.getSelectedIndex();
                if(sel > 0){
                    moveEntry(-1, sel);
                }
            } else if(o == movedown){
                int sel = list.getSelectedIndex();
                if(sel < entries.length - 1){
                    moveEntry(1, sel);
                }
            } else if(o == add){
                addEntry();
            } else if(o == remove){
                removeEntry();
            } else if(o == edit){
                editEntry();
            }
        }
        
        private void addControls(){
            JPanel buttonsPanel = new JPanel(new GridLayout(5, 1));
            setLayout(new BorderLayout());
            
            buttonsPanel.add(moveup = new JButton("Move up"));
            buttonsPanel.add(movedown = new JButton("Move down"));
            buttonsPanel.add(add = new JButton("Add"));
            buttonsPanel.add(remove = new JButton("Remove"));
            buttonsPanel.add(edit = new JButton("Edit"));
            
            add(buttonsPanel, BorderLayout.EAST);
            add(new JScrollPane(list = new JList()));
            
            moveup.addActionListener(this);
            movedown.addActionListener(this);
            add.addActionListener(this);
            remove.addActionListener(this);
            edit.addActionListener(this);
            
            list.setCellRenderer(renderer);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    }
    
    public class FilterRenderer implements ListCellRenderer {
        private DefaultListCellRenderer left;
        private JPanel comp;
        
        public FilterRenderer(){
            comp = new JPanel(new BorderLayout());
            left = new DefaultListCellRenderer();

            comp.add(left);

            Font font = new Font("SansSerif", Font.PLAIN, 11);
            Border border = BorderFactory.createEtchedBorder();
            comp.setBorder(border);
            left.setFont(font);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
            left.setComponentOrientation(list.getComponentOrientation());
            if (isSelected) {
                left.setBackground(list.getSelectionBackground());
                left.setForeground(list.getSelectionForeground());
            } else {
                left.setBackground(list.getBackground());
                left.setForeground(list.getForeground());
            }
            
            if(value != null){
                FilterEntry entry = (FilterEntry)value;
                StringBuilder content = new StringBuilder();
                content.append("<html><body>");
                content.append("<table border=0 width=100%><tr><td>");
                content.append("<i>SRC> </i>");
                if(!entry.isSrcIPSet()){
                    content.append("<s>");
                }
                getIPRange(content, entry.getSrc_lip(), entry.getSrc_uip());
                if(!entry.isSrcIPSet()){
                    content.append("</s>");
                }
                content.append(":");
                if(!entry.isSrcPortSet()){
                    content.append("<s>");
                }
                getPortRange(content, entry.getSrc_lport(), entry.getSrc_uport());
                if(!entry.isSrcPortSet()){
                    content.append("</s>");
                }
                content.append("<br>");
                content.append("<i>DST> </i>");
                if(!entry.isDstIPSet()){
                    content.append("<s>");
                }
                getIPRange(content, entry.getDst_lip(), entry.getDst_uip());
                if(!entry.isDstIPSet()){
                    content.append("</s>");
                }
                content.append(":");
                if(!entry.isDstPortSet()){
                    content.append("<s>");
                }
                getPortRange(content, entry.getDst_lport(), entry.getDst_uport());
                if(!entry.isDstPortSet()){
                    content.append("</s>");
                }
                content.append("</td></tr></table>");
                content.append("</body></html>");
                comp.setToolTipText("Summary: " + entry.getSpecificSummary());
                left.setText(content.toString());
            } else {
                left.setText("");
            }
            
            left.setEnabled(list.isEnabled());
            
            return comp;
        }
        
        private String getPortRange(StringBuilder builder, int start, int end){
            if(start != end){
                builder.append("[" + start);
                builder.append(" - ");
                builder.append(end + "]");
            } else {
                builder.append(start + "");
            }
            
            return builder.toString();
        }
        
        private String getIPRange(StringBuilder builder, int[] start, int[] end){
            for(int i = 0; i < start.length; i++){
                int s = start[i];
                int e = end[i];
                
                if(s != e){
                    builder.append("[" + s + "-" + e + "]");
                } else {
                    builder.append(s + "");
                }
                
                if(i != start.length - 1){
                    builder.append(".");
                }
            }
            
            return builder.toString();
        }
    }
}