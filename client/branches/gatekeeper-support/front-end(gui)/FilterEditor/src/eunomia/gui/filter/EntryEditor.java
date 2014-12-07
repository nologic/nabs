/*
 * EntryEditor.java
 *
 * Created on January 9, 2006, 8:35 PM
 *
 */

package eunomia.gui.filter;

import com.vivic.eunomia.module.receptor.FlowModule;
import eunomia.flow.FilterEntry;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EntryEditor extends JDialog implements ActionListener {
    private JButton ok, cancel;
    private EntryEditorPanel editor;
    
    public EntryEditor(Window owner){
        super(owner, "Filter Entry Editor", Dialog.DEFAULT_MODALITY_TYPE);
        
        setSize(430, 450);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        
        addControls();
    }
    
    public void setEntry(FilterEntry e, FlowModule fmod) {
        editor.setEntry(e, fmod);
    }
    
    private void cancel(){
        setVisible(false);
    }
    
    private void ok() {
        editor.commit();
        setVisible(false);
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == ok){
            ok();
        } else if(o == cancel){
            cancel();
        }
    }

    private void addControls() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        buttonsPanel.add(ok = new JButton("OK"));
        buttonsPanel.add(cancel = new JButton("Cancel"));
        c.add(editor = new EntryEditorPanel());
        c.add(bottomPanel, BorderLayout.SOUTH);

        ok.addActionListener(this);
        cancel.addActionListener(this);
    }
}