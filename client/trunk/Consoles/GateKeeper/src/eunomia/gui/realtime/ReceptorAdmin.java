/*
 * ReceptorAdmin.java
 *
 * Created on December 12, 2005, 10:47 PM
 *
 */

package eunomia.gui.realtime;

import eunomia.core.receptor.Receptor;
import eunomia.gui.LoginDialog;
import eunomia.gui.MainGui;
import eunomia.gui.PassChangeDialog;
import eunomia.gui.realtime.receptorAdmin.StreamsPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ReceptorAdmin extends JPanel implements ActionListener {
    private static Font labelFont;

    private Receptor receptor;
    private JButton update;
    private StreamsPanel sPanel;
    private JButton changePass, showLegend;
    private PassChangeDialog passDialog;
    
    static {
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
    }
    
    public ReceptorAdmin(Receptor rec) {
        receptor = rec;
        passDialog = new PassChangeDialog("root", false);
        
        addControls();
    
        receptor.getOutComm().updateReceptor();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == changePass) {
            passDialog.setLocationRelativeTo(changePass);
            if(passDialog.askPassword(true) == PassChangeDialog.SUBMITED) {
                String oldPass = passDialog.getPassword();
                String newPass = passDialog.getNewPassword();
                passDialog.setPassword("");
                passDialog.setNewPassword("");
                receptor.getOutComm().setReceptorUser("root", newPass, oldPass);
            }
        } else if(o == showLegend) {
            MainGui.v().showColorSettings();
        }
    }

    private void addControls(){
        JPanel buttons = new JPanel(new GridLayout(2, 1));
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        JPanel legend = new JPanel(new BorderLayout(0, 20));
        
        setLayout(new BorderLayout());
        
        panel.add(changePass = new JButton("Change Password"));
        legend.add(showLegend = new JButton("Show Legend"));
        
        buttons.add(panel);
        buttons.add(legend);
        
        add(sPanel = new StreamsPanel(receptor));
        add(buttons, BorderLayout.SOUTH);
        
        sPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sensors:"));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Sieve:"));
        legend.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Legend:"));

        receptor.getState().addReceptorStateListener(sPanel);
        changePass.addActionListener(this);
        showLegend.addActionListener(this);
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Sensor Settings"));
    }
    
    public static JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        label.setHorizontalAlignment(JLabel.CENTER);
        
        return label;
    }
}