/*
 * TerminalManager.java
 *
 * Created on July 5, 2005, 3:24 PM
 *
 */

package eunomia.gui.archival;

import eunomia.core.receptor.Receptor;
import eunomia.gui.interfaces.FrameCreator;
import eunomia.messages.DatabaseDescriptor;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.HashMap;
import javax.swing.JInternalFrame;

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
    
    public void openTerminal(DatabaseDescriptor db, Receptor rec){
        String dbName = db.getName();
        JInternalFrame frame = (JInternalFrame)dbToTerm.get(dbName);
        
        if(frame == null){
            frame = creator.createInterfaceFrame();
            dbToTerm.put(dbName, frame);

            Container c = frame.getContentPane();
            c.setLayout(new BorderLayout());

            DatabaseTerm term = new DatabaseTerm(dbName, rec);
            c.add(term);
            
            rec.getState().addTerminal(term, dbName);

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