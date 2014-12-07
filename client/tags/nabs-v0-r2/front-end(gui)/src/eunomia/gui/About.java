package eunomia.gui;

import eunomia.NABStrings;
import java.awt.*;
import javax.swing.*;

public class About extends JDialog {

    
    public About(JFrame frame){
        super(frame, true);
        setSize(340, 240);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle(NABStrings.ABOUT_WINDOW_TITLE);
        
        JLabel textLabel = new JLabel(NABStrings.ABOUT_CONTENT);
        
        setContentPane(new JPanel());
        Container c = getContentPane();
        try {
            c.setBackground(Color.decode("0xFFFFF0"));
        } catch(Exception e){
            e.printStackTrace();
        }
        c.add(textLabel);
    }
}