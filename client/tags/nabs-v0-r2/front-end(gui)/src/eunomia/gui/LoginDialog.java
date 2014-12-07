/*
 * LoginDialog.java
 *
 * Created on August 12, 2006, 8:45 PM
 *
 */

package eunomia.gui;

import eunomia.util.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LoginDialog extends JDialog implements ActionListener, KeyListener {
    public static final int CANCELED = 0, SUBMITED = 1;
    
    private JButton ok, cancel;
    private JTextField user, pass;
    private int retValue;
    
    public LoginDialog() {
        setModal(true);
        setSize(300, 150);
        setLocationRelativeTo(MainGui.v());
        setTitle("Login Credentials");
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        addControls();
    }
    
    public int askPassword(boolean force){
        if(force || user.getText().equals("")){
            setVisible(true);
        }
        
        return retValue;
    }
    
    public String getPassword(){
        return pass.getText();
    }
    
    public String getUsername(){
        return user.getText();
    }
    
    private void setRetValue(int r){
        retValue = r;
        setVisible(false);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == ok){
            setRetValue(SUBMITED);
        } else if(o == cancel){
            setRetValue(CANCELED);
        }
    }
    
    private void addControls(){
        JPanel buttons = new JPanel(new SpringLayout());
        JPanel credent = new JPanel(new SpringLayout());
        
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        
        buttons.add(ok = new JButton("Submit"));
        buttons.add(cancel = new JButton("Cancel"));
        SpringUtilities.makeCompactGrid(buttons, 1, 2, 6, 6, 6, 6);
        
        credent.add(new JLabel("Username:"));
        credent.add(user = new JTextField());
        credent.add(new JLabel("Password:"));
        credent.add(pass = new JPasswordField());
        SpringUtilities.makeCompactGrid(credent, 2, 2, 6, 6, 6, 10);
        
        c.add(buttons, BorderLayout.SOUTH);
        c.add(credent);
        
        ok.addActionListener(this);
        cancel.addActionListener(this);
        
        user.addKeyListener(this);
        pass.addKeyListener(this);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch(code){
            case KeyEvent.VK_ESCAPE: 
                setRetValue(CANCELED);
                break;
            case KeyEvent.VK_ENTER:
                setRetValue(SUBMITED);
                break;
        }
    }
}
