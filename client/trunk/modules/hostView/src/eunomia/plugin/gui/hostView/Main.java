/*
 * Main.java
 *
 * Created on January 21, 2006, 3:57 PM
 *
 */

package eunomia.plugin.gui.hostView;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleContext;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.msg.AddRemoveHostMessage;
import eunomia.plugin.msg.OpenDetailsMessage;
import com.vivic.eunomia.sys.util.Util;
import eunomia.util.number.ModLong;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements FrontendProcessorModule, ActionListener {
    private ConsoleReceptor receptor;
    private JPanel displayPanel;
    private JPanel hostsHolder;
    private JPanel controlPanel;
    private JList hostList;
    private JButton addHost;
    private JButton removeHost;
    private LinkedList hosts;
    private HashMap ipToHost;
    private ModLong tmpLong;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        hosts = new LinkedList();
        ipToHost = new HashMap();
        tmpLong = new ModLong();
        
        setUpControlPanel();
        setUpDisplayPanel();
        
        this.receptor = ConsoleContext.getReceptor();
    }
    
    private void setUpDisplayPanel(){
        JScrollPane mainScroll;
        JPanel mainPanel = new JPanel(new BorderLayout());
        displayPanel = new JPanel(new BorderLayout());
        hostsHolder = new JPanel();
        
        hostsHolder.setLayout(new BoxLayout(hostsHolder, BoxLayout.Y_AXIS));
        mainPanel.add(hostsHolder, BorderLayout.NORTH);
        displayPanel.add(mainScroll = new JScrollPane(mainPanel));
        
        JScrollBar bar = mainScroll.getVerticalScrollBar();
        mainScroll.setBorder(null);
        bar.setBlockIncrement(40);
        bar.setUnitIncrement(40);
    }
    
    private void setUpControlPanel(){
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        controlPanel = new JPanel(new BorderLayout());
        
        buttonsPanel.add(addHost = new JButton("Add Host"));
        buttonsPanel.add(removeHost = new JButton("Remove Host"));
        
        controlPanel.add(new JLabel("Monitored Hosts"), BorderLayout.NORTH);
        controlPanel.add(new JScrollPane(hostList = new JList()));
        controlPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        addHost.addActionListener(this);
        removeHost.addActionListener(this);
        
        hostList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == addHost){
            addHost();
        } else if(o == removeHost){
            removeHost();
        }
    }
    
    public void addHostToDetails(long ip, ModuleHandle handle){
        OpenDetailsMessage odm = new OpenDetailsMessage();
        odm.setHandle(handle);
        odm.setIp(ip);
        
        try {
            ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
            oo.writeObject(odm);
            oo.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void updateList(){
        hostList.setListData(hosts.toArray());
    }
    
    private void removeHost(){
        InetAddress addr = (InetAddress)hostList.getSelectedValue();
        if(addr != null){
            sendAddRemoveHost(addr, false);
        }
    }
    
    private void addHost(){
        String str = JOptionPane.showInputDialog(controlPanel, "Enter Host IP/Name:");
        if(str != null){
            try {
                sendAddRemoveHost(InetAddress.getByName(str), true);
            } catch(UnknownHostException uh){
                logger.error(uh.getMessage());
            } catch(Exception e){
                e.printStackTrace();
                logger.error("String " + str + " is not valid");
            }
        }
    }
    
    public void addHost(InetAddress host){
        addHost(Util.getLongIp(host), host);
    }
    
    private void addHost(long ip, InetAddress host){
        Long ident = new Long(ip);
        
        if(ipToHost.containsKey(ident)){
            logger.info("Host " + host + " is already on the list");
            return;
        }
        
        Host hostViewer = new Host(ip, host, this);
        hostsHolder.add(hostViewer);
        
        ipToHost.put(ident, hostViewer);
        hosts.add(host);
        
        updateList();
    }
    
    private void removeHost(long ip){
        Long ident = new Long(ip);
        
        Host hostViewer = (Host)ipToHost.get(ident);
        if(hostViewer == null){
            return;
        }
        
        hostsHolder.remove(hostViewer);
        ipToHost.remove(ident);
        hosts.remove(hostViewer.getHost());
        
        updateList();
    }
    
    private void sendAddRemoveHost(InetAddress host, boolean doAdd){
        AddRemoveHostMessage arhm = new AddRemoveHostMessage();
        arhm.setDoAdd(doAdd);
        arhm.setIp(Util.getLongIp(host));
        
        try {
            ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
            oo.writeObject(arhm);
            oo.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public JComponent getControlComponent() {
        return controlPanel;
    }

    public void getControlData(OutputStream out) throws java.io.IOException {
    }

    public JComponent getJComponent() {
        return displayPanel;
    }

    public Object getProperty(String name) {
        return null;
    }

    public ConsoleReceptor getReceptor() {
        return receptor;
    }

    public String getTitle() {
        return "Host View";
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void setProperty(String name, Object value) {
    }

    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        Set curSet = new HashSet(ipToHost.keySet());
        
        int count = din.readInt();
        for(int i = 0; i < count; i++){
            long ip = din.readLong();
            tmpLong.setLong(ip);
            Host host = (Host)ipToHost.get(tmpLong);
            if(host == null){
                addHost(ip, Util.getInetAddress(ip));
                host = (Host)ipToHost.get(tmpLong);
            }
            
            curSet.remove(tmpLong);
            
            host.readHost(din);
            host.updateData();
        }
        
        Iterator it = curSet.iterator();
        while (it.hasNext()) {
            Long elem = (Long)it.next();
            removeHost(elem.longValue());
        }
        
        hostsHolder.revalidate();
        hostsHolder.repaint();
    }

    public void processMessage(DataInputStream din) throws IOException {
    }
}