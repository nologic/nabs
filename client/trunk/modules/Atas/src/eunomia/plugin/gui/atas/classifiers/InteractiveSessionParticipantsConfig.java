/*
 * InteractiveSessionParticipantsConfig.java
 *
 * Created on April 11, 2007, 9:47 PM
 *
 */

package eunomia.plugin.gui.atas.classifiers;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.ConfigurationInterface;
import eunomia.plugin.rec.atas.classifiers.msg.InteractiveSessionConfigMessage;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Mikhail Sosonkin
 */
public class InteractiveSessionParticipantsConfig extends JPanel implements ConfigurationInterface {
    private InteractiveSessionConfigMessage msg;
    private JTextField alpha;
    private JTextField beta;
    private JTextField epsilon;
    
    public InteractiveSessionParticipantsConfig() {
        setLayout(new GridLayout(1, 6));
        
        add(new JLabel("Alpha: "));
        add(alpha = new JTextField());
        add(new JLabel("Beta: "));
        add(beta = new JTextField());
        add(new JLabel("Epsilon: "));
        add(epsilon = new JTextField());
    }

    public void setConfigurationMessage(ClassifierConfigurationMessage m) {
        msg = (InteractiveSessionConfigMessage)m;
        
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), msg.getRoleNumber() + " - " + msg.getRoleName()));
        alpha.setText(Double.toString(msg.getAlpha()));
        beta.setText(Double.toString(msg.getBeta()));
        epsilon.setText(Double.toString(msg.getEpsilon()));
    }

    public ClassifierConfigurationMessage getConfigurationMessage() {
        msg.setAlpha(Double.parseDouble(alpha.getText()));
        msg.setBeta(Double.parseDouble(beta.getText()));
        msg.setEpsilon(Double.parseDouble(epsilon.getText()));
        
        return msg;
    }
}