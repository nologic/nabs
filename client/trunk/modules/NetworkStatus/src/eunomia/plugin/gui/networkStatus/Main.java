/*
 * MainLoaded.java
 *
 * Created on May 20, 2007, 3:36 PM
 *
 */

package eunomia.plugin.gui.networkStatus;

import com.vivic.eunomia.sys.frontend.ConsoleContext;
import eunomia.plugin.gui.lossyHistogram.MessageSender;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.plugin.msg.ModifyGraphMessage;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main extends JPanel implements FrontendProcessorModule {
    private static final byte LOSSY1 = 0x0;
    private static final byte LOSSY2 = 0x1;
    private static final byte PIECHT = 0x2;
    private static final byte HOSTDT = 0x3;
    
    private ConsoleReceptor receptor;
    private JComponent controlPanel;
    
    private eunomia.plugin.gui.pieChart.Main pieChart;
    private eunomia.plugin.gui.lossyHistogram.Main lcUploaders;
    private eunomia.plugin.gui.lossyHistogram.Main lcDownloaders;
    private eunomia.plugin.gui.hostDetails.Main hostDetails;
    
    public Main() {
        lcUploaders = new eunomia.plugin.gui.lossyHistogram.Main();
        lcDownloaders = new eunomia.plugin.gui.lossyHistogram.Main();
        hostDetails = new eunomia.plugin.gui.hostDetails.Main();
        pieChart = new eunomia.plugin.gui.pieChart.Main();
        
        setLayout(new GridLayout(2, 2));
        
        add(lcUploaders.getJComponent());
        add(lcDownloaders.getJComponent());
        add(hostDetails.getJComponent());
        add(pieChart.getJComponent());
        revalidate();
        repaint();
        
        lcUploaders.showLegend(false);
        lcDownloaders.showLegend(false);
        
        String helpText = "" +
                "<HTML><BODY>" +
                "<TABLE width=400> <tr> <td>" +
                "<font size=4 face=Verdana>" +
                "Welcome to the Network Abuse Detection System Console. There are many useful features available, " +
                "they are accessible through the tabs at the bottom:" +
                "</font>" +
                "<ul>" +
                "    <li><u>Network Status</u> - (This window) provides general network information. To get specific host information, use the above graph." +
                "                             Right-click on a specific host and choose 'Move to Details'." +
                "    <li><u>Feedback/Questions</u> - can be used for directly communicating with the Vivic Networks. If you have questions/comments feel free to use" +
                "                                   this feature.</li>" +
                "    <li><u>Network Policy</u> - is used for detecting organizational network use policy violations.</li>" +
                "</ul>" +
                "<font size=4 face=Verdana>Side Panel:</font>" +
                "<ul>" +
                "    <li><u>Sensor Settings</u> - use the panel to configure the remote sensors.</li>" +
                "</ul>" +
                "</td> </tr> </table>" +
                "</BODY></HTML>";
        
        hostDetails.setProperty(eunomia.plugin.gui.hostDetails.Main.CMD_SET_MSG_SENDER, new HSD_MsgSender(this));
        hostDetails.setProperty(eunomia.plugin.gui.hostDetails.Main.CMD_SET_INIT_LABEL, helpText);
        lcUploaders.setProperty(eunomia.plugin.gui.lossyHistogram.Main.CMD_SET_MSG_SENDER, new LC1_MsgSender(this));
        lcDownloaders.setProperty(eunomia.plugin.gui.lossyHistogram.Main.CMD_SET_MSG_SENDER, new LC2_MsgSender(this));
        lcUploaders.setProperty(eunomia.plugin.gui.lossyHistogram.Main.CMD_SINGLE_MOVE, "Details");
        lcDownloaders.setProperty(eunomia.plugin.gui.lossyHistogram.Main.CMD_SINGLE_MOVE, "Details");
        
        /*receptor = ConsoleContext.getReceptor();
        lcUploaders.setReceptor(receptor);
        lcDownloaders.setReceptor(receptor);
        pieChart.setReceptor(receptor);
        hostDetails.setReceptor(receptor);*/
    }

    public JComponent getJComponent() {
        return this;
    }

    public JComponent getControlComponent() {
        if(controlPanel == null) {
            JPanel control;
            JTabbedPane tabs = new JTabbedPane();
            
            JPanel countersTab = new JPanel(new BorderLayout());
            controlPanel = new JPanel(new BorderLayout());
            
            JPanel cntPanel = new JPanel(new GridLayout(2, 1));
            control = new JPanel(new BorderLayout());
            control.add(lcUploaders.getControlComponent());
            control.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Top Hosts #1"));
            cntPanel.add(control);
            
            control = new JPanel(new BorderLayout());
            control.add(lcDownloaders.getControlComponent());
            control.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Top Hosts #2"));
            cntPanel.add(control);
            countersTab.add(cntPanel, BorderLayout.NORTH);
        
            tabs.addTab("Top Host Counters", new JScrollPane(countersTab));
            tabs.addTab("Detailed View", hostDetails.getControlComponent());
            
            controlPanel.add(tabs);
        }
        
        return controlPanel;
    }

    public String getTitle() {
        return "Network Status";
    }

    public void processMessage(DataInputStream in) throws IOException {
        byte b = (byte)in.read();
        
        switch(b) {
            case LOSSY1:
                lcUploaders.processMessage(in);
                break;
                
            case LOSSY2:
                lcDownloaders.processMessage(in);
                break;
                
            case PIECHT:
                break;
                
            case HOSTDT:
                hostDetails.processMessage(in);
                break;
        }
    }

    public void setProperty(String name, Object value) {
        hostDetails.setProperty(name, value);
    }

    public Object getProperty(String name) {
        return null;
    }

    public void updateStatus(InputStream in) throws IOException {
        lcUploaders.updateStatus(in);
        lcDownloaders.updateStatus(in);
        pieChart.updateStatus(in);
        hostDetails.updateStatus(in);
    }

    public void getControlData(OutputStream out) throws IOException {
        lcUploaders.getControlData(out);
        lcDownloaders.getControlData(out);
        pieChart.getControlData(out);
        hostDetails.getControlData(out);
    }

    public void setControlData(InputStream in) throws IOException {
        lcUploaders.setControlData(in);
        lcDownloaders.setControlData(in);
        pieChart.setControlData(in);
        hostDetails.setControlData(in);
    }

    public void sendObject(Object o, int mod) throws IOException {
        OutputStream ostream = receptor.getManager().openInterModuleStream(this);
        ostream.write(mod);
        
        ObjectOutput oo = new ObjectOutputStream(ostream);
        oo.writeObject(o);
        oo.close();
    }
    
    private class LC1_MsgSender implements MessageSender {
        private Main main;
        
        public LC1_MsgSender(Main m) {
            main = m;
        }
        
        public void sendObject(Object o) throws IOException {
            main.sendObject(o, LOSSY1);
        }
    }
    
    private class LC2_MsgSender implements MessageSender {
        private Main main;
        
        public LC2_MsgSender(Main m) {
            main = m;
        }
        
        public void sendObject(Object o) throws IOException {
            main.sendObject(o, LOSSY2);
        }
    }

    private class HSD_MsgSender implements eunomia.plugin.gui.hostDetails.MessageSender {
        private Main main;
        
        public HSD_MsgSender(Main m) {
            main = m;
        }
        
        public void sendObject(Object o) throws IOException {
            main.sendObject(o, Main.HOSTDT);
        }
    }
}