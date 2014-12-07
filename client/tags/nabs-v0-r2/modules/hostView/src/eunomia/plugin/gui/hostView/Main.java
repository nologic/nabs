/*
 * Main.java
 *
 * Created on January 21, 2006, 3:57 PM
 *
 */

package eunomia.plugin.gui.hostView;

import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.flow.Filter;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import eunomia.plugin.interfaces.GUIModule;
import eunomia.flow.*;
import eunomia.core.receptor.*;
import eunomia.messages.Message;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import eunomia.plugin.msg.AddRemoveHostMessage;
import eunomia.plugin.msg.OpenDetailsMessage;
import eunomia.util.Util;
import eunomia.util.number.ModLong;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements GUIModule, MessageReceiver, ActionListener {
    private Receptor receptor;
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
        
        GenericModuleMessage gmm = receptor.getManager().prepareGenericMessage(this);
        try {
            ObjectOutput oo = new ObjectOutputStream(gmm.getOutputStream());
            oo.writeObject(odm);
            receptor.getManager().sendGenericMessage(this, gmm);
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
        
        GenericModuleMessage gmm = receptor.getManager().prepareGenericMessage(this);
        try {
            ObjectOutput oo = new ObjectOutputStream(gmm.getOutputStream());
            oo.writeObject(arhm);
            receptor.getManager().sendGenericMessage(this, gmm);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////

    public boolean allowFilters() {
        return true;
    }

    public boolean allowFullscreen() {
        return true;
    }

    public boolean allowToolbar() {
        return true;
    }

    public JComponent getControlComponent() {
        return controlPanel;
    }

    public void getControlData(OutputStream out) throws java.io.IOException {
    }

    public Filter getFilter() {
        return null;
    }

    public JComponent getJComponent() {
        return displayPanel;
    }

    public Object getProperty(String name) {
        return null;
    }

    public Receptor getReceptor() {
        return receptor;
    }

    public MessageReceiver getReceiver() {
        return this;
    }

    public String getTitle() {
        return "Host View";
    }

    public boolean isControlSeparate() {
        return true;
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void setProperty(String name, Object value) {
    }

    public void setReceptor(Receptor receptor) {
        this.receptor = receptor;
    }

    public void showLegend(boolean b) {
    }

    public void showTitle(boolean b) {
    }

    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        Set curSet =  new HashSet(ipToHost.keySet());
        
        int count = din.readInt();
        for(int i = 0; i < count; i++){
            long ip = din.readLong();
            tmpLong.setLong(ip);
            Host host = (Host)ipToHost.get(tmpLong);
            if(host == null){
                addHost(ip, Util.getInetAddress(ip));
                host = (Host)ipToHost.get(tmpLong);
            } else {
                curSet.remove(tmpLong);
            }
            
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

    public void messageResponse(Message msg) {
    }
}