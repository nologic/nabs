/*
 * DarkSpaceRoleConfig.java
 *
 * Created on April 11, 2007, 8:04 PM
 *
 */

package eunomia.plugin.gui.atas.classifiers;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.ConfigurationInterface;
import eunomia.plugin.rec.atas.classifiers.msg.DarkSpaceConfigMessage;
import com.vivic.eunomia.sys.util.Util;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DarkSpaceRoleConfig extends JPanel implements ConfigurationInterface {
    private DarkSpaceConfigMessage msg;
    private JTextField ip1;
    private JTextField ip2;
    
    public DarkSpaceRoleConfig() {
        setLayout(new GridLayout(2, 2));
        
        add(new JLabel("IP Range Beginning: "));
        add(ip1 = new JTextField());
        add(new JLabel("IP Range Ending: "));
        add(ip2 = new JTextField());
    }
    
    public void setConfigurationMessage(ClassifierConfigurationMessage m) {
        this.msg = (DarkSpaceConfigMessage)m;
        
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), msg.getRoleNumber() + " - " + msg.getRoleName()));
        ip1.setText(Util.ipToString(msg.getIpRangeBegin()));
        ip2.setText(Util.ipToString(msg.getIpRangeEnd()));
    }

    public ClassifierConfigurationMessage getConfigurationMessage() {
        msg.setIpRangeBegin(Util.getLongIp(ip1.getText()));
        msg.setIpRangeEnd(Util.getLongIp(ip2.getText()));
        
        return msg;
    }
}
