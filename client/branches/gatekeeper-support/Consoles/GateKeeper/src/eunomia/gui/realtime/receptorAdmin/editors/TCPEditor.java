/*
 * TCPEditor.java
 *
 * Created on August 27, 2006, 1:38 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin.editors;

import eunomia.gui.realtime.ReceptorAdmin;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.messages.receptor.protocol.impl.TCPProtocol;
import eunomia.util.SpringUtilities;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author Mikhail Sosonkin
 */
public class TCPEditor extends JPanel implements ProtocolEditor {
    private TCPProtocol descriptor;
    private JTextField ip, port;
    
    public TCPEditor() {
        descriptor = new TCPProtocol();
        addControls();
    }

    public ProtocolDescriptor getDescriptor() {
        descriptor.setIp(ip.getText());
        try {
            descriptor.setPort(Integer.parseInt(port.getText()));
        } catch (Exception e){
            descriptor.setPort(0);
        }
        
        return descriptor;
    }

    public void setDescriptor(ProtocolDescriptor d) {
        if(d instanceof TCPProtocol){
            TCPProtocol tcp = (TCPProtocol)d;
            ip.setText(tcp.getIp());
            port.setText(tcp.getPort() + "");
        }
    }
    
    private void addControls(){
        setLayout(new SpringLayout());
        
        add(ReceptorAdmin.makeLabel("            "));
        add(ReceptorAdmin.makeLabel("TCP Protocol"));
        add(ReceptorAdmin.makeLabel("IP:"));
        add(ip = new JTextField());
        add(ReceptorAdmin.makeLabel("Port:"));
        add(port = new JTextField());
        
        SpringUtilities.makeCompactGrid(this, 3, 2, 2, 2, 4, 4);
    }
    
    public String toString() {
        return "TCP";
    }
}