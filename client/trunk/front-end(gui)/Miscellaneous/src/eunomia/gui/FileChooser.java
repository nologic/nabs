/*
 * FileChooser.java
 *
 * Created on August 22, 2005, 4:23 PM
 *
 */

package eunomia.gui;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Mikhail Sosonkin
 */

public class FileChooser {
    private static final FileChooser ins = new FileChooser();
    
    private JFileChooser chooser;
    private Component parent;
    
    private FileChooser() {
        chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setSize(400, 300);
        chooser.setControlButtonsAreShown(true);
    }
    
    private File getOpenFileV(Component comp, String title, String apprString, File baseDir, 
            int mode, javax.swing.filechooser.FileFilter filter){
        
        if(comp == null){
            comp = parent;
        }
        
        if(baseDir != null){
            chooser.setCurrentDirectory(baseDir);
        }
        
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setApproveButtonText(apprString);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(mode);

        if(filter != null){
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(filter);
        } else {
            chooser.setAcceptAllFileFilterUsed(true);
        }
        
        if(chooser.showOpenDialog(comp) == JFileChooser.APPROVE_OPTION){
            return chooser.getSelectedFile();
        }
        
        return null;
    }
    
    private File getSaveFileV(){
        return null;
    }
    
    public static void setParent(Component p){
        ins.parent = p;
    }
    
    public static File getOpenFile(){
        return ins.getOpenFileV(null, "Open file", "Open", null,
                JFileChooser.FILES_ONLY, null);
    }
    
    public static File getOpenFile(javax.swing.filechooser.FileFilter filter){
        return ins.getOpenFileV(null, "Open file", "Open", null,
                JFileChooser.FILES_ONLY, filter);
    }
    
    public static File getSaveFile(){
        return null;
    }
}