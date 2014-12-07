/*
 * Main.java
 *
 * Created on April 17, 2006, 8:32 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.flow.Filter;
import eunomia.messages.Message;
import eunomia.messages.module.msg.GenericModuleMessage;
import eunomia.plugin.interfaces.GUIModule;
import eunomia.plugin.msg.hostDetails.AddRemoveHostMessage;
import eunomia.plugin.msg.hostDetails.HostListMessage;
import eunomia.util.Util;
import eunomia.util.number.ModLong;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements GUIModule, MessageReceiver, ActionListener {
    private DetailsPanel dPanel;
    private Receptor receptor;
    private long[] hosts;
    private boolean isUpdating;
    private HashMap hostMap;
    private ModLong ipLong;
    private JPanel controlPanel;
    private JList hostList;
    private JButton addHost;
    private JButton removeHost;
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        hostMap = new HashMap();
        dPanel = new DetailsPanel();
        ipLong = new ModLong();
        isUpdating = false;
        setUpControlPanel();
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
    
    private void updateControlList(){
        InetAddress[] addrs = new InetAddress[hosts.length];
        for(int i = 0; i < addrs.length; ++i){
            addrs[i] = Util.getInetAddress(hosts[i]);
        }
        hostList.setListData(addrs);
    }
    
    private HostDetail addHost(long ip){
        ipLong.setLong(ip);
        
        HostDetail hDetail = (HostDetail)hostMap.get(ipLong);
        if(hDetail == null){
            hDetail = new HostDetail(ip);
            ModLong key = new ModLong();
            key.setLong(ip);
            hostMap.put(key, hDetail);
            dPanel.showHost(hDetail);
        }
        
        return hDetail;
    }
    
    private void removeHost(HostDetail hd){
        ipLong.setLong(hd.getHostIp());
        hostMap.remove(ipLong);
        dPanel.removeHost(hd);
    }

    private void updateAvailableHosts(){
        if(!isUpdating){
            isUpdating = true;
            Set allNow = new HashSet(hostMap.values());
            for (int i = 0; i < hosts.length; i++) {
                allNow.remove(addHost(hosts[i]));
            }
            
            //clean up
            Iterator it = allNow.iterator();
            while (it.hasNext()) {
                HostDetail hd = (HostDetail) it.next();
                removeHost(hd);
            }
            
            updateControlList();
            
            HostListMessage hlm = new HostListMessage();
            hlm.setList(hosts);

            GenericModuleMessage gmm = receptor.getManager().prepareGenericMessage(this);
            try {
                ObjectOutput oo = new ObjectOutputStream(gmm.getOutputStream());
                oo.writeObject(hlm);
                receptor.getManager().sendGenericMessage(this, gmm);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private void readHosts(DataInputStream din) throws IOException {
        while(din.available() != 0){
            ipLong.setLong(din.readLong());
            HostDetail hDetail = (HostDetail)hostMap.get(ipLong);
            if(hDetail == null){
                addHost(ipLong.longValue());
                hDetail = (HostDetail)hostMap.get(ipLong);
            }
            
            hDetail.readIn(din);
        }
    }
    
    public JComponent getJComponent() {
        return dPanel;
    }

    public JComponent getControlComponent() {
        return controlPanel;
    }

    public String getTitle() {
        return "Host Datails";
    }

    public Filter getFilter() {
        return null;
    }

    public MessageReceiver getReceiver() {
        return this;
    }

    public boolean allowFullscreen() {
        return true;
    }

    public boolean allowFilters() {
        return true;
    }

    public boolean allowToolbar() {
        return true;
    }

    public boolean isControlSeparate() {
        return true;
    }

    public void showLegend(boolean b) {
    }

    public void showTitle(boolean b) {
    }

    public void setReceptor(Receptor rec) {
        receptor = rec;
    }

    public Receptor getReceptor() {
        return receptor;
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        hosts = new long[in.available() / 8];
        for (int i = 0; i < hosts.length; i++) {
            hosts[i] = din.readLong();
        }
        updateAvailableHosts();
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void messageResponse(Message msg) {
        if(msg instanceof GenericModuleMessage){
            InputStream in = ((GenericModuleMessage)msg).getInputStream();
            try {
                if(in.read() == 0xFE && in.read() == 0xED){
                    readHosts(new DataInputStream(in));
                    isUpdating = false;
                } else {
                    // Some message
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == addHost){
            addHost();
        } else if(o == removeHost){
            removeHost();
        }
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
}