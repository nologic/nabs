/*
 * FileChooserTextField.java
 *
 * Created on July 16, 2006, 11:24 PM
 *
 */

package eunomia.gui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;

/**
 *
 * @author Mikhail Sosonkin
 */
public class FileChooserTextField extends JPanel implements ActionListener {
    private JTextField field;
    private JButton browse;
    private String extension;
    private FF filter;
    
    public FileChooserTextField() {
        this(null, null, 0);
    }
    
    public FileChooserTextField(String text, int columns) {
        this(null, text, columns);
    }
    
    public FileChooserTextField(int columns) {
        this(null, null, columns);
    }
    
    public FileChooserTextField(String text) {
        this(null, text, 0);
    }
    
    public FileChooserTextField(Document doc, String text, int columns) {
        this(doc, text, columns, "");
    }
    
    public FileChooserTextField(Document doc, String text, int columns, String fileExt) {
        setExtension(fileExt.toLowerCase());
        filter = new FF();
        
        setLayout(new BorderLayout());
        
        add(field = new JTextField(doc, text, columns));
        add(browse = new JButton("Browse"), BorderLayout.EAST);
        
        browse.addActionListener(this);
    }
    
    public void setText(String text){
        field.setText(text);
    }
    
    public String getText(){
        return field.getText();
    }
    
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension.toLowerCase();
    }
    
    private void setFile(){
        FileChooser.setParent(this);
        File choice = FileChooser.getOpenFile(filter);
        if(choice != null){
            field.setText(choice.toString());
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == browse){
            setFile();
        }
    }
    
    private class FF extends FileFilter {
        public boolean accept(File f) {
            return f.toString().toLowerCase().endsWith(getExtension()) || f.isDirectory();
        }

        public String getDescription() {
            return "(*." + getExtension() + ")";
        }
    }
}