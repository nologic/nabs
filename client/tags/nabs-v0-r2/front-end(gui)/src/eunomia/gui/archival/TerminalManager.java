/*
 * TerminalManager.java
 *
 * Created on July 5, 2005, 3:24 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.receptor.Receptor;
import eunomia.gui.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TerminalManager {
    private static TerminalManager ins;
    
    private FrameCreator creator;
    private HashMap dbToTerm;

    public TerminalManager(FrameCreator fc) {
        creator = fc;
        dbToTerm = new HashMap();
    }
    
    public void openTerminal(String dbName, Receptor rec){
        JInternalFrame frame = (JInternalFrame)dbToTerm.get(dbName);
        
        if(frame == null){
            frame = creator.createInterfaceFrame();
            dbToTerm.put(dbName, frame);

            Container c = frame.getContentPane();
            c.setLayout(new BorderLayout());

            c.add(new DatabaseTerminal(dbName, rec));

            frame.setTitle("Terminal for " + dbName);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        }
        
        frame.setVisible(true);
        frame.toFront();
        try {
            frame.setSelected(true);
        } catch(Exception ex){
        }
    }
}