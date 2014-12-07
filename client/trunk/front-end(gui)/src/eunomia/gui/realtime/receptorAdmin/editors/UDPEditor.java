/*
 * UDPProcol.java
 *
 * Created on August 27, 2006, 2:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.gui.realtime.receptorAdmin.editors;

import eunomia.gui.realtime.ReceptorAdmin;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import eunomia.messages.receptor.protocol.impl.UDPProtocol;
import eunomia.util.SpringUtilities;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UDPEditor extends JPanel implements ProtocolEditor {
    private UDPProtocol descriptor;
    private JTextField port;
    
    public UDPEditor() {
        descriptor = new UDPProtocol();
        addControls();
    }

    public ProtocolDescriptor getDescriptor() {
        descriptor.setListenPort(Integer.parseInt(port.getText()));
        
        return descriptor;
    }

    public void setDescriptor(ProtocolDescriptor d) {
        if(d instanceof UDPProtocol){
            UDPProtocol tcp = (UDPProtocol)d;
            port.setText(tcp.getListenPort() + "");
        }
    }
    
    private void addControls(){
        setLayout(new SpringLayout());
        
        add(ReceptorAdmin.makeLabel("            "));
        add(ReceptorAdmin.makeLabel("UDP Protocol"));
        add(ReceptorAdmin.makeLabel("Port:"));
        add(port = new JTextField());
        
        SpringUtilities.makeCompactGrid(this, 2, 2, 2, 2, 4, 4);
    }
    
    public String toString() {
        return "UDP";
    }
}