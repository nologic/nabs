/*
 * EntryEditor.java
 *
 * Created on January 9, 2006, 8:35 PM
 *
 */

package eunomia.gui.filter;

import eunomia.core.managers.FlowModuleManager;
import eunomia.flow.FilterEntry;
import eunomia.gui.*;
import eunomia.receptor.module.interfaces.FilterEntryEditor;
import eunomia.receptor.module.interfaces.FlowModule;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EntryEditor extends JDialog implements ActionListener {
    private static Border ipBorder;
    
    private AddressEditor src1, src2;
    private AddressEditor dst1, dst2;
    private FilterEntry entry;
    private FilterEntryEditor editor;
    private JButton ok, cancel;
    private JCheckBox useSrcIp, useSrcPort, useDstIp, useDstPort;
    private JCheckBox srcSingle, dstSingle;
    private JPanel specificEditor;
    
    static {
        ipBorder = BorderFactory.createLineBorder(Color.BLACK);
    }
    
    public EntryEditor(Dialog owner){
        super(owner, "Filter Entry Editor", true);
        
        setSize(430, 450);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        
        addControls();
    }
    
    private void ok(){
        if(srcSingle.isSelected()){
            src2.setInetAddress(src1.getAddress(), src1.getPort());
        }
        
        if(dstSingle.isSelected()){
            dst2.setInetAddress(dst1.getAddress(), dst1.getPort());
        }
        
        entry.setSourceIpRange(src1.getAddress(), src2.getAddress());
        entry.setSourcePortRange(src1.getPort(), src2.getPort());
        entry.setDestinationIpRange(dst1.getAddress(), dst2.getAddress());
        entry.setDestinationPortRange(dst1.getPort(), dst2.getPort());
        
        entry.setIsSrcIPSet(useSrcIp.isSelected());
        entry.setIsSrcPortSet(useSrcPort.isSelected());
        entry.setIsDstIPSet(useDstIp.isSelected());
        entry.setIsDstPortSet(useDstPort.isSelected());
        
        editor.commitChanges();
        
        setVisible(false);
    }
    
    private void cancel(){
        setVisible(false);
    }
    
    private void scanUses(){
        src1.setIPEnabled(useSrcIp.isSelected());
        src1.setPortEnabled(useSrcPort.isSelected());
        if(srcSingle.isSelected()){
            src2.setPortEnabled(false);
            src2.setIPEnabled(false);
        } else {
            src2.setPortEnabled(useSrcPort.isSelected());
            src2.setIPEnabled(useSrcIp.isSelected());
        }
        
        dst1.setIPEnabled(useDstIp.isSelected());
        dst1.setPortEnabled(useDstPort.isSelected());
        if(dstSingle.isSelected()){
            dst2.setPortEnabled(false);
            dst2.setIPEnabled(false);
        } else {
            dst2.setPortEnabled(useDstPort.isSelected());
            dst2.setIPEnabled(useDstIp.isSelected());
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == ok){
            ok();
        } else if(o == cancel){
            cancel();
        } else if(o instanceof JCheckBox){
            scanUses();
        }
    }
    
    public void setEntry(FilterEntry e){
        int[] ip1, ip2;
        entry = e;
        
        specificEditor.removeAll();
        FlowModule fmod = FlowModuleManager.ins.getFlowModuleInstance(e.getModuleName());
        if(fmod != null){
            editor = fmod.getFilterEditor();
            editor.setFilterEntry(e);
            specificEditor.add(editor.getComponent());
        }
        
        src1.setInetAddress(e.getSrc_lip(), e.getSrc_lport());
        src2.setInetAddress(e.getSrc_uip(), e.getSrc_uport());
        dst1.setInetAddress(e.getDst_lip(), e.getDst_lport());
        dst2.setInetAddress(e.getDst_uip(), e.getDst_uport());
        
        useSrcIp.setSelected(e.isSrcIPSet());
        useSrcPort.setSelected(e.isSrcPortSet());
        useDstIp.setSelected(e.isDstIPSet());
        useDstPort.setSelected(e.isDstPortSet());
        
        srcSingle.setSelected(true);
        if(e.getSrc_lport() != e.getSrc_uport()){
            srcSingle.setSelected(false);
        }
        
        ip1 = e.getSrc_lip();
        ip2 = e.getSrc_uip();
        for(int i = 0; i < ip1.length; ++i){
            if(ip1[i] != ip2[i]){
                srcSingle.setSelected(false);
                break;
            }
        }
        
        dstSingle.setSelected(true);
        if(e.getDst_lport() != e.getDst_uport()){
            dstSingle.setSelected(false);
        }
        
        ip1 = e.getDst_lip();
        ip2 = e.getDst_uip();
        for(int i = 0; i < ip1.length; ++i){
            if(ip1[i] != ip2[i]){
                dstSingle.setSelected(false);
                break;
            }
        }
        
        scanUses();
    }
    
    private void addControls(){
        JPanel sourcePanel = new JPanel(new BorderLayout());
        JPanel destinationPanel = new JPanel(new BorderLayout());
        JPanel mainPanel = new JPanel();
        JPanel sourceAddressPanel = new JPanel(new GridLayout(2, 1));
        JPanel destinationAddressPanel = new JPanel(new GridLayout(2, 1));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel srcUsePanel = new JPanel(new GridLayout(2, 2));
        JPanel dstUsePanel = new JPanel(new GridLayout(2, 2));
        JPanel editorPanels = new JPanel(new BorderLayout());
        Container c = getContentPane();
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        c.setLayout(new BorderLayout());
        
        srcUsePanel.add(useSrcIp = new JCheckBox("Use Source IP Range"));
        srcUsePanel.add(srcSingle = new JCheckBox("Use As One Address"));
        srcUsePanel.add(useSrcPort = new JCheckBox("Use Source Port Range"));
        srcUsePanel.add(new JPanel());
        sourceAddressPanel.add(src1 = new AddressEditor());
        sourceAddressPanel.add(src2 = new AddressEditor());
        sourcePanel.add(srcUsePanel, BorderLayout.NORTH);
        sourcePanel.add(sourceAddressPanel);
        
        dstUsePanel.add(useDstIp = new JCheckBox("Use Destination IP Range"));
        dstUsePanel.add(dstSingle = new JCheckBox("Use As One Address"));
        dstUsePanel.add(useDstPort = new JCheckBox("Use Destination Port Range"));
        dstUsePanel.add(new JPanel());
        destinationAddressPanel.add(dst1 = new AddressEditor());
        destinationAddressPanel.add(dst2 = new AddressEditor());
        destinationPanel.add(dstUsePanel, BorderLayout.NORTH);
        destinationPanel.add(destinationAddressPanel);
        
        mainPanel.add(sourcePanel);
        mainPanel.add(new JPanel());
        mainPanel.add(destinationPanel);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        buttonsPanel.add(ok = new JButton("OK"));
        buttonsPanel.add(cancel = new JButton("Cancel"));
        
        editorPanels.add(mainPanel, BorderLayout.NORTH);
        editorPanels.add(new JScrollPane(specificEditor = new JPanel(new BorderLayout())));
        add(editorPanels);
        add(bottomPanel, BorderLayout.SOUTH);
        
        ok.addActionListener(this);
        cancel.addActionListener(this);
        
        useSrcIp.addActionListener(this);
        useSrcPort.addActionListener(this);
        srcSingle.addActionListener(this);
        dstSingle.addActionListener(this);
        useDstIp.addActionListener(this);
        useDstPort.addActionListener(this);
        
        specificEditor.setBorder(BorderFactory.createTitledBorder(" Flow Specific "));
        sourcePanel.setBorder(BorderFactory.createTitledBorder(" Source Range "));
        destinationPanel.setBorder(BorderFactory.createTitledBorder(" Destination Range "));
    }
    
    private class AddressEditor extends JPanel {
        private IpByte[] ipFields;
        private JTextField portField;
        
        public AddressEditor(){
            addControls();
        }
        
        public void setIPEnabled(boolean b){
            for(int i = 0; i < ipFields.length; ++i){
                ipFields[i].setEnabled(b);
            }
        }
        
        public void setPortEnabled(boolean b){
            portField.setEditable(b);
        }
        
        public void setEnabled(boolean b){
            setIPEnabled(b);
            setPortEnabled(b);
            super.setEnabled(b);
        }
        
        public void setInetAddress(int[] add, int port){
            for(int i = 0; i < add.length; ++i){
                ipFields[i].setNumber(add[i] & 0xFF);
            }
            
            portField.setText(port + "");
        }
        
        public int getPort(){
            try {
                return Integer.parseInt(portField.getText());
            } catch(Exception e){
                e.printStackTrace();
                return 0;
            }
        }
        
        public int[] getAddress(){
            int[] ipBytes = new int[4];
            
            for(int i = 0; i < 4; ++i){
                ipBytes[i] = ipFields[i].getNumber();
            }
            
            return ipBytes;
        }
        
        private void addControls(){
            NumberDocument doc;
            JPanel mainPanel = new JPanel(new GridLayout(1, 5));
            setLayout(new BorderLayout());
            
            ipFields = new IpByte[4];
            for(int i = 0; i < 4; ++i){
                mainPanel.add(ipFields[i] = new IpByte());
            }
            
            ipFields[3].setSep(":");
            mainPanel.add(portField = new TheTextField());
            add(mainPanel, BorderLayout.NORTH);
            
            portField.setHorizontalAlignment(JTextField.CENTER);
            portField.setDocument(doc = new NumberDocument(5, 65535));
            portField.setBorder(null);
            doc.setJTextField(portField);
            
            mainPanel.setBorder(ipBorder);
        }
        
        private class TheTextField extends JTextField implements FocusListener {
            public TheTextField(){
                addFocusListener(this);
            }
            
            public void focusGained(FocusEvent e) {
                String txt = getText();
                if(txt.length() > 0){
                    setSelectionStart(0);
                    setSelectionEnd(txt.length());
                }
            }
            
            public void focusLost(FocusEvent e) {
                String txt = getText();
                if(txt.equals("")){
                    setText("0");
                }
            }
        }
        
        private class IpByte extends JPanel {
            private JTextField field;
            private JLabel sep;
            
            public IpByte() {
                addControls();
            }
            
            public void setEnabled(boolean b){
                field.setEditable(b);
                sep.setEnabled(b);
                sep.setBackground(field.getBackground());
                super.setEnabled(b);
            }
            
            public int getNumber(){
                String txt = field.getText();
                if(txt.equals("")){
                    txt = "0";
                }
                try {
                    return Integer.parseInt(txt);
                } catch(Exception e){
                    e.printStackTrace();
                    return 0;
                }
            }
            
            public void setNumber(int num){
                field.setText(num + "");
            }
            
            public void setSep(String s){
                sep.setText(s);
            }
            
            private void addControls(){
                NumberDocument doc;
                setLayout(new BorderLayout());
                
                add(field = new TheTextField());
                add(sep = new JLabel("."), BorderLayout.EAST);
                
                sep.setBackground(field.getBackground());
                sep.setOpaque(true);
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setDocument(doc = new NumberDocument());
                field.setBorder(null);
                doc.setJTextField(field);
            }
        }
    }
    
    private class NumberDocument extends PlainDocument {
        private int maxLen = 3;
        private int maxNum = 255;
        private JTextField textField;
        
        public NumberDocument() {
            this(3, 255);
        }
        
        public void setJTextField(JTextField field){
            textField = field;
        }
        
        public NumberDocument(int ml, int mn) {
            maxLen = ml;
            maxNum = mn;
        }
        
        public void setMaxLen(int l){
            maxLen = l;
        }
        
        public void setMaxNum(int num){
            maxNum = num;
        }
        
        public void insertString(int pos, String str, AttributeSet attributeSet)  throws BadLocationException {
            int len = this.getLength();
            // not a proper way, but JComponent won't let me use a char for this.
            if(textField != null && str.equals(".")){
                textField.transferFocus();
                return;
            }
            
            if (str.length() > 0) {
                if(len + str.length() > maxLen){
                    return;
                }
                
                try {
                    String cur = this.getText(0, len);
                    
                    int i = Integer.parseInt(cur + str);
                    if(i > maxNum){
                        return;
                    }
                } catch (Exception nfe) {
                    return;
                }
            }
            
            super.insertString(pos, str, attributeSet);
        }
    }
}