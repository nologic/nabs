/*
 * TerminalManager.java
 *
 * Created on July 5, 2005, 3:24 PM
 *
 */

package eunomia.gui.archival;

import eunomia.gui.*;
import eunomia.core.data.staticData.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TerminalManager {
    private FrameCreator creator;
    private HashMap dbToTerm;

    public TerminalManager(FrameCreator fc) {
        creator = fc;
        dbToTerm = new HashMap();
    }
    
    public void openTerminal(Database db){
        JInternalFrame frame = (JInternalFrame)dbToTerm.get(db);
        
        if(frame == null){
            frame = creator.createInterfaceFrame();
            dbToTerm.put(db, frame);

            Container c = frame.getContentPane();
            c.setLayout(new BorderLayout());

            c.add(new DatabaseTerminal(db));

            frame.setTitle("Terminal for " + db);
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