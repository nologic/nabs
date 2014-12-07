/*
 * StreamsPanel.java
 *
 * Created on January 1, 2006, 11:41 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.gui.realtime.ReceptorAdmin;
import eunomia.gui.realtime.receptorAdmin.editors.ProtocolEditor;
import eunomia.gui.realtime.receptorAdmin.editors.TCPEditor;
import eunomia.messages.receptor.protocol.impl.TCPProtocol;
import eunomia.util.SpringUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StreamsPanel extends JPanel implements ActionListener, ReceptorStateListener {

    private Receptor receptor;
    private JButton update, addStream, removeStream, editSensor, closeDialog;
    private JTextField name;
    private JPanel pSpecPanel;
    private ProtocolEditor editor;
    private TCPEditor tcpEditor = new TCPEditor();
    private JPanel streamsPanel;
    private Map streamToSensor;
    private Sensor curSensor;
    private JPanel editContainer;
    private EditDialog editorDialog;
    
    public StreamsPanel(Receptor rec) {
        receptor = rec;
        streamToSensor = new HashMap();
        
        addControls();
    }

    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();
        
        if(o == addStream){
            if(editor != null && !name.getText().equals("")){
                receptor.getOutComm().addStream(name.getText(), "NABFlow", editor.getDescriptor());
            }
            editorDialog.setVisible(false);
        } else if(o == removeStream){
            receptor.getOutComm().removeStream(name.getText());
        } else if(o == update) {
            String addr = JOptionPane.showInputDialog(update, "Enter IP:Port of the sensor", "Sensor Quick Add", JOptionPane.QUESTION_MESSAGE);

            if(addr != null) {
                if(addr.indexOf(':') == -1) {
                    JOptionPane.showMessageDialog(update, "Invalid IP:Port format. \n(ex. 127.0.0.1:1234)");
                    return;
                }
                
                String split[] = addr.split(":");
                TCPProtocol d = (TCPProtocol)editor.getDescriptor();
                d.setIp(split[0]);
                d.setPort(Integer.parseInt(split[1]));
                receptor.getOutComm().addStream(addr, "NABFlow", d);
            }
        } else if(o == editSensor) {
            if(curSensor == null && streamToSensor.size() != 0) {
                JOptionPane.showMessageDialog(editSensor, "Select a sensor");
                return;
            }
            
            if(editorDialog == null) {
                editorDialog = new EditDialog(this);
                editorDialog.setContentPane(editContainer);
            }
            
            editorDialog.setLocationRelativeTo(editSensor);
            editorDialog.setVisible(true);
        } else if(o == closeDialog) {
            editorDialog.setVisible(false);
        }
    }
    
    private void connectStream(String name, boolean con){
        receptor.getOutComm().connectStream(name, con);
    }
    
    private void addControls(){
        JPanel controlsPanel = new JPanel(new BorderLayout());
        JPanel fieldsPanel = new JPanel(new SpringLayout());
        JPanel protocolSpecific = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        JPanel streamEdit = new JPanel(new BorderLayout());
        JPanel sPanel = new JPanel(new BorderLayout());
        JPanel dialogButtons = new JPanel(new GridLayout(1, 2));
        JPanel dialogBottom = new JPanel(new BorderLayout());
        JPanel dialogMain = new JPanel(new GridLayout(1, 2));
        
        editContainer = new JPanel(new BorderLayout());
        
        setLayout(new BorderLayout());
        
        buttonsPanel.add(update = new JButton("Quick Add"));
        buttonsPanel.add(removeStream = new JButton("Remove Selected"));
        buttonsPanel.add(editSensor = new JButton("Edit Selected"));
        controlsPanel.add(buttonsPanel, BorderLayout.SOUTH);

        fieldsPanel.add(new JLabel("Name: "));
        fieldsPanel.add(name = new JTextField());
        SpringUtilities.makeCompactGrid(fieldsPanel, 2, 1, 2, 2, 4, 4);
        
        protocolSpecific.add(new JScrollPane(pSpecPanel = new JPanel(new BorderLayout())));
        
        dialogButtons.add(addStream = new JButton("Save Changes"));
        dialogButtons.add(closeDialog = new JButton("Cancel"));
        dialogBottom.add(dialogButtons, BorderLayout.EAST);
        
        streamEdit.add(fieldsPanel, BorderLayout.NORTH);
        dialogMain.add(streamEdit);
        dialogMain.add(protocolSpecific);
        editContainer.add(dialogMain);
        editContainer.add(dialogBottom, BorderLayout.SOUTH);
        
        streamsPanel = new JPanel();
        streamsPanel.setLayout(new BoxLayout(streamsPanel, BoxLayout.Y_AXIS));
        sPanel.add(streamsPanel, BorderLayout.NORTH);
        
        JScrollPane pane;
        add(controlsPanel, BorderLayout.SOUTH);
        add(pane = new JScrollPane(sPanel));
        
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        addStream.addActionListener(this);
        removeStream.addActionListener(this);
        update.addActionListener(this);
        editSensor.addActionListener(this);
        closeDialog.addActionListener(this);
        pSpecPanel.add((JComponent)(editor = tcpEditor), BorderLayout.NORTH);
    }

    public void databaseAdded(AddDatabaseEvent e) {
    }

    public void databaseRemoved(RemoveDatabaseEvent e) {
    }

    public void databaseTypeAdded(AddDatabaseTypeEvent e) {
    }

    public void moduleAdded(AddModuleEvent e) {
    }

    public void streamServerAdded(AddStreamServerEvent e) {
        Sensor s = (Sensor)streamToSensor.get(e.getServer().getName());
        if(s == null) {
            s = new Sensor();
            streamToSensor.put(e.getServer().getName(), s);
            streamsPanel.add(s);
            s.setServer(e.getServer());
            s.setSelected(false);
        }
        s.setServer(e.getServer());
        editSensor.setText("Edit Selected");
    }

    public void streamServerRemoved(RemoveStreamServerEvent e) {
        Sensor s = (Sensor)streamToSensor.remove(e.getServer().getName());
        streamsPanel.remove(s);
        if(streamToSensor.size() == 0) {
            editSensor.setText("Add Sensor");
        }
    }

    public void streamStatusChanged(StreamStatusChangedEvent e) {
        Sensor s = (Sensor)streamToSensor.get(e.getServer().getName());
        s.setServer(e.getServer());
        s.setSelected(s == curSensor);
    }

    public void receptorUserAdded(ReceptorUserAddedEvent e) {
    }

    public void receptorUserRemoved(ReceptorUserRemovedEvent e) {
    }
    
    private void sensorSelected(Sensor s) {
        s.setSelected(true);
        
        if(curSensor != null) {
            curSensor.setSelected(false);
        }
        
        curSensor = s;
        name.setText(s.getServer().getName());
        tcpEditor.setDescriptor(s.getServer().getProtocol());
    }
    
    private static Border commonUnselBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);
    private static Border commonSelecBorder = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    private static Color selectColor = Color.GREEN;
    private static Color unselectColor = new JPanel().getBackground();
    
    private class Sensor extends JPanel implements ActionListener, MouseListener {
        private JCheckBox active;
        private StreamServerDesc serv;
        private JLabel label;
        
        public Sensor() {
            setLayout(new BorderLayout());

            add(active = new JCheckBox("Active"), BorderLayout.NORTH);
            add(label = new JLabel(""));
            
            label.setOpaque(false);
            active.setOpaque(false);
            active.addActionListener(this);
            addMouseListener(this);
        }
        
        public void setServer(StreamServerDesc stream) {
            serv = stream;
            
            label.setText(stream.getProtocol().toString());
            active.setSelected(stream.isConnected());
            setBackground(stream.isConnected()?selectColor:unselectColor);
        }
        
        public StreamServerDesc getServer() {
            return serv;
        }

        public void actionPerformed(ActionEvent e) {
            sensorSelected(this);
            connectStream(serv.getName(), active.isSelected());
            active.setSelected(!active.isSelected());
        }
        
        public void setSelected(boolean b) {
            setBorder(BorderFactory.createTitledBorder((b?commonSelecBorder:commonUnselBorder), serv.getName()));
            repaint();
        }

        public void mouseClicked(MouseEvent e) {
            sensorSelected(this);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
    
    private static class EditDialog extends JDialog {
        
        public EditDialog(Component owner) {
            super(JOptionPane.getFrameForComponent(owner), "Sensor Editor", true);
            
            setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            setSize(400, 150);
        }
    }
}