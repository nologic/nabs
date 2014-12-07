/*
 * Main.java
 *
 * Created on April 17, 2006, 8:32 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleContext;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.plugin.msg.hostDetails.AddRemoveHostMessage;
import eunomia.plugin.msg.hostDetails.HostListMessage;
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
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements FrontendProcessorModule, ActionListener, MessageSender {
    public static String CMD_SET_MSG_SENDER = "CMD_SET_MSG_SENDER";
    public static String CMD_SET_INIT_LABEL = "CMD_SET_INIT_LABEL";
    
    private DetailsPanel dPanel;
    private ConsoleReceptor receptor;
    private long[] hosts;
    private boolean isUpdating;
    private HashMap hostMap;
    private ModLong ipLong;
    private JComponent controlPanel;
    private JList hostList;
    private JButton addHost;
    private JButton removeHost;
    private MessageSender sender;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        sender = this;
        hostMap = new HashMap();
        dPanel = new DetailsPanel(this);
        ipLong = new ModLong();
        isUpdating = false;
        setUpControlPanel();
        
        receptor = ConsoleContext.getReceptor();
    }
    
    private void setUpControlPanel(){
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        buttonsPanel.add(addHost = new JButton("Add Host"));
        buttonsPanel.add(removeHost = new JButton("Remove Host"));
        bottomPanel.add(buttonsPanel, BorderLayout.WEST);
        
        mainPanel.add(new JLabel("Monitored Hosts"), BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(hostList = new JList()));
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        addHost.addActionListener(this);
        removeHost.addActionListener(this);
        
        hostList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        controlPanel = new JScrollPane(mainPanel);
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
            hDetail = new HostDetail(ip, receptor);
            ModLong key = new ModLong();
            key.setLong(ip);
            hostMap.put(key, hDetail);
            dPanel.showHost(hDetail);

            if(hostMap.size() > 20) {
                logger.warn("There are a lot of hosts (" + hostMap.size() + ") in detailed view, consider purging unneeded hosts down to 20.");
            }
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

            try {
                sender.sendObject(hlm);
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
                hDetail = addHost(ipLong.longValue());
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
        return "Host Details";
    }

    public void setProperty(String name, Object value) {
        if(name.equals("AH")) {
            try {
                sendAddRemoveHost(Long.parseLong(value.toString()), true);
            } catch(Exception e){
                e.printStackTrace();
                logger.error("String " + value + " is not valid");
            }
        } else if(name.equals("AHD")) {
            try {
                long ip = Long.parseLong(value.toString());
                logger.info("Showing host " + Util.ipToString(ip));
                
                HostDetail host = addHost(ip);
                sendAddRemoveHost(ip, true);
                dPanel.detach(host);
            } catch(Exception e){
                e.printStackTrace();
                logger.error("String " + value + " is not valid");
            }
        } else if(name.equals(CMD_SET_MSG_SENDER)) {
            sender = (MessageSender)value;
        } else if(name.equals(CMD_SET_INIT_LABEL)) {
            dPanel.setInstText(value.toString());
        }
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
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == addHost){
            addHost(controlPanel);
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
    
    void addHost(JComponent parent){
        String str = JOptionPane.showInputDialog(parent, "Enter Host IP/Name:");
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
        sendAddRemoveHost(Util.getLongIp(host), doAdd);
    }
    
    private void sendAddRemoveHost(long ip, boolean doAdd){
        AddRemoveHostMessage arhm = new AddRemoveHostMessage();
        arhm.setDoAdd(doAdd);
        arhm.setIp(ip);
        
        try {
            sender.sendObject(arhm);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void processMessage(DataInputStream din) throws IOException {
        try {
            if(din.read() == 0xFE && din.read() == 0xED){
                readHosts(din);
                isUpdating = false;
            } else {
                // Some message
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendObject(Object o) throws IOException {
        ObjectOutput oo = new ObjectOutputStream(receptor.getManager().openInterModuleStream(this));
        oo.writeObject(o);
        oo.close();
    }
}