package eunomia.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.text.*;

public class About extends JDialog {
    private String aboutString = "" +
            "<html>" +
            "<body link=\"#000099\" bgcolor=\"#FFFFF0\">" +
            "<center><big><big><strong><font color=\"#800080\">" +
            "The NABS client</font></strong></big></big><br>" +
            "<hr width=\"75%\">" +
            "<br>" +
            "<u>Trinetra LLC.</u>" +
            "<br>" +
            "<br>" +
            "<strong>Contact:</strong><br>" +
            "Nasir Memon<br>" +
            "<a href=\"mailto:memon@poly.edu\">memon@poly.edu</a><br>" +
            "<br>" +
            "<small>Version 1.0 Alpha, September 2005</small><br>" +
            "</center>" +
            "</body>" +
            "</html>";
    
    public About(JFrame frame){
        super(frame, true);
        setSize(340, 240);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setTitle("About NABS Client");
        
        JLabel textLabel = new JLabel(aboutString);
        
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