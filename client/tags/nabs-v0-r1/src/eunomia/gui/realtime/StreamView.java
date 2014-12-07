/*
 * StreamView.java
 *
 * Created on June 17, 2005, 6:07 PM
 */

package eunomia.gui.realtime;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import eunomia.core.data.streamData.*;
import eunomia.core.data.streamData.listeners.*;
import eunomia.core.managers.DataManager;
import eunomia.plugin.interfaces.Module;
import eunomia.plugin.streamStatus.StreamStatusModule;
import org.apache.log4j.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamView extends JPanel implements ActionListener, StreamChangeListener, Runnable {
    private StreamDataSource stream;
    private RealtimePanel rtPanel;
    private JCheckBox activate;
    private JLabel status;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StreamView.class);
    }
    
    public StreamView(StreamDataSource sds) {
        stream = sds;
        rtPanel = new RealtimePanel(sds);
        addControls();
    }
    
    public RealtimePanel getRealtimePanel(){
        return rtPanel;
    }
    
    public void run(){
        try {
            status.setText("Connecting...");
            stream.setActive(activate.isSelected());
        } catch(Exception ex){
            ex.printStackTrace();
            logger.error(stream + " - Unable to connect -  " + ex.getMessage());
            activate.setSelected(!activate.isSelected());
        }
        status.setText("");
        activate.setEnabled(true);
    }
    
    public void activate(){
        activate.setEnabled(false);
        new Thread(this).start();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o == activate){
            activate();
        }
    }

    public void streamStateChanged() {
    }

    private void addControls(){
        setLayout(new BorderLayout());
        add(activate = new JCheckBox("Activate"), BorderLayout.EAST);
        add(status = new JLabel());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), stream.toString()));
        
        activate.addActionListener(this);
        status.setHorizontalAlignment(JLabel.CENTER);
        
        Module ssm = new StreamStatusModule();
        stream.registerRaw(ssm.getFlowPocessor());
        DataManager.ins.registerWithUpdater(ssm.getRefreshNotifier());
        add(ssm.getJComponent(), BorderLayout.SOUTH);
    }
}