/*
 * StreamConfigurationPanel.java
 *
 * Created on June 15, 2005, 5:13 PM
 */

package eunomia.gui.settings;

import eunomia.core.managers.ReceptorManager;
import eunomia.core.managers.listeners.DatabaseManagerListener;
import eunomia.core.managers.listeners.ReceptorManagerListener;
import eunomia.gui.NABStrings;
import eunomia.core.receptor.Receptor;
import eunomia.util.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import org.apache.log4j.Logger;


/**
 *
 * @author  Mikhail Sosonkin
 */
public class ReceptorConfigurationPanel extends JPanel implements ActionListener, 
            DatabaseManagerListener, ReceptorManagerListener {
    
    private JButton apply;
    private JLabel name, isCollecting, isActive, isLossy;
    private JTextField serverIP, serverPort, rate;
    private Receptor receptor;
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(ReceptorConfigurationPanel.class);
    }
    
    public ReceptorConfigurationPanel() {
        addControls();
        
        Iterator it = ReceptorManager.ins.getReceptors().iterator();
        while(it.hasNext()){
            streamAddedNoFire((Receptor)it.next());
        }
        updateSourcesPanel();
    }

    private void applySettings(){
        if(receptor == null){
            return;
        }
        
        int port = 0;
        String ip = serverIP.getText();
        try {
            port = Integer.parseInt(serverPort.getText());
            receptor.setIPPort(ip, port);
            receptor.setRefreshRate(Integer.parseInt(rate.getText()));
            ReceptorManager.ins.save();
        } catch(NumberFormatException e){
            logger.error("Unable to parse port string: " + e.getMessage());
        }
    }
    
    private void updateSourcesPanel(){
        validate();
        repaint();
    }
        
    private void streamAddedNoFire(Receptor rec) {
        JCheckBox box = new JCheckBox(rec.toString());
        box.addActionListener(this);
    }
    
    public void receptorAdded(Receptor rec) {
        streamAddedNoFire(rec);
        updateSourcesPanel();
    }
    
    public void receptorRemoved(Receptor rec) {
        updateSourcesPanel();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == apply){
            applySettings();
        }
    }

    public void setReceptor(Receptor rec){
        if(rec == null){
            return;
        }
                
        setEnabled(true);
        receptor = rec;
        name.setText(rec.getName());

        serverIP.setText(rec.getIP());
        serverPort.setText(rec.getPort() + "");
        rate.setText(rec.getRefreshRate() + "");
    }
    
    private void addControls(){
        JPanel configPanel = new JPanel(new BorderLayout());
        JPanel fieldsPanel = new JPanel(new SpringLayout());
        JPanel buttonsPanel = new JPanel(new SpringLayout());
        
        setLayout(new BorderLayout());
        
        fieldsPanel.add(new JLabel(NABStrings.CURRENT_RECEPTOR_NAME + " name:"));
        fieldsPanel.add(name = new JLabel());
        fieldsPanel.add(new JLabel("Server IP:"));
        fieldsPanel.add(serverIP = new JTextField());
        fieldsPanel.add(new JLabel("Server Port:"));
        fieldsPanel.add(serverPort = new JTextField());
        fieldsPanel.add(new JLabel("Refresh rate:"));
        fieldsPanel.add(rate = new JTextField());
        
        buttonsPanel.add(apply = new JButton("Apply changes"));
        
        SpringUtilities.makeCompactGrid(buttonsPanel, buttonsPanel.getComponentCount(), 1, 40, 0, 0, 0);
        SpringUtilities.makeCompactGrid(fieldsPanel, fieldsPanel.getComponentCount()/2, 2, 20, 6, 6, 6);

        configPanel.add(fieldsPanel, BorderLayout.NORTH);
                
        add(configPanel);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        fieldsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Configuration"));
        
        apply.addActionListener(this);
    }
}