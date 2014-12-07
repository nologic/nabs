/*
 * FilterEditorPanel.java
 *
 * Created on September 30, 2007, 7:29 PM
 *
 */

package eunomia.gui.filter;

import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.filter.FilterEntry;
import com.vivic.eunomia.filter.FilterList;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FilterEditorPanel extends JPanel {
    private FilterRenderer renderer;
    private ListPanel bList;
    private ListPanel wList;
    private Filter filter;
    private EntryEditor editor;
    private JPanel contentPane;
    private String[] fmodNames;
    private ConsoleModuleManager modMan;
    private boolean isModified;

    public FilterEditorPanel() {
        renderer = new FilterRenderer();
        
        addControls();
    }
    
    public boolean isModified() {
        return isModified;
    }
    
    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }
    
    public void setFilter(Filter f, ConsoleModuleManager man, String[] fmodNames){
        isModified = false;
        this.fmodNames = fmodNames;
        modMan = man;
        filter = f;
        bList.setList(f.getBlackList());
        wList.setList(f.getWhiteList());
    }
    
    public Filter getFilter() {
        return filter;
    }
    
    public EntryEditor getEntryEditor() {
        if(editor == null) {
            editor = new EntryEditor(JOptionPane.getFrameForComponent(this));
        }
        
        return editor;
    }
    
    private void addControls(){
        JPanel listsPanel = new JPanel(new GridLayout(2, 1));
        JPanel wListPanel = new JPanel(new BorderLayout());
        JPanel bListPanel = new JPanel(new BorderLayout());
        
        Container c = this;
        c.setLayout(new BorderLayout());
        
        wListPanel.add(new JLabel("White List"), BorderLayout.NORTH);
        wListPanel.add(wList = new ListPanel());
        bListPanel.add(new JLabel("Black List"), BorderLayout.NORTH);
        bListPanel.add(bList = new ListPanel());
        listsPanel.add(wListPanel);
        listsPanel.add(bListPanel);
        
        c.add(listsPanel);
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
            EntryEditor ee = getEntryEditor();
            if(entry != null){
                ee.setEntry(entry, modMan.getFlowModule(entry.getModuleName()));
                
                ee.setLocationRelativeTo(this);
                ee.setVisible(true);
                updateList();
            }
        }
        
        private void addEntry(){
            String[] choices = fmodNames;
            Object selection = null;
            if(choices.length > 1){
                selection = JOptionPane.showInputDialog(this, "Select Flow Type", "Module Selection", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            } else {
                selection = choices[0];
            }
            
            if(selection != null){
                FlowModule fmod = modMan.getFlowModule(selection.toString());
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
            isModified = true;
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
}
