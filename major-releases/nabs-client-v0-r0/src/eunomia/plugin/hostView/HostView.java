/*
 * HostView.java
 *
 * Created on August 9, 2005, 4:20 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.core.data.flow.*;
import eunomia.core.data.streamData.StreamDataSource;
import eunomia.plugin.interfaces.*;
import eunomia.util.number.*;

import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.apache.log4j.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HostView implements Module, ModularFlowProcessor, RefreshNotifier, ActionListener {
    private JPanel displayPanel;
    private JPanel hostsHolder;
    private JPanel controlPanel;
    private JList hostList;
    private Filter filter;
    private HashMap ipToHost;
    private ModLong tmpLong;
    private LinkedList hosts;
    
    private JButton addHost;
    private JButton removeHost;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(HostView.class);
    }
    
    public HostView() {
        filter = new Filter();
        ipToHost = new HashMap();
        tmpLong = new ModLong();
        hosts = new LinkedList();
        
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
    
    private void updateList(){
        hostList.setListData(hosts.toArray());
    }
    
    private void removeHost(){
        InetAddress addr = (InetAddress)hostList.getSelectedValue();
        if(addr != null){
            removeHost(addr);
        }
    }
    
    private void addHost(){
        String str = JOptionPane.showInputDialog(controlPanel, "Enter Host IP/Name:");
        if(str != null){
            try {
                addHost(InetAddress.getByName(str));
            } catch(UnknownHostException uh){
                logger.error(uh.getMessage());
            } catch(Exception e){
                e.printStackTrace();
                logger.error("String " + str + " is not valid");
            }
        }
    }
    
    public void removeHost(InetAddress host){
        long ip;
        Long ident;

        ip = getLongIp(host);
        ident = new Long(ip);
        
        Host hostViewer = (Host)ipToHost.remove(ident);
                
        if(hostViewer == null){
            return;
        }
        
        hostsHolder.remove(hostViewer);
        filter.removeFilterWhite(hostViewer.getEntryDst());
        filter.removeFilterWhite(hostViewer.getEntrySrc());
        hosts.remove(host);
        hostViewer.hostRemoved();
        updateList();
    }
    
    public void addHost(InetAddress host){
        long ip;
        Long ident;

        ip = getLongIp(host);
        ident = new Long(ip);
        
        if(ipToHost.containsKey(ident)){
            logger.info("Host " + host + " is already on the list");
            return;
        }
        
        Host hostViewer = new Host(ip, host);
        hostsHolder.add(hostViewer);
        
        FilterEntry entrySrc = new FilterEntry();
        FilterEntry entryDst = new FilterEntry();
        entrySrc.setSourceIpRange(host, host);
        entryDst.setDestinationIpRange(host, host);
        hostViewer.setEntryDst(entryDst);
        hostViewer.setEntrySrc(entrySrc);
        filter.addFilterWhite(entrySrc);
        filter.addFilterWhite(entryDst);
        
        ipToHost.put(ident, hostViewer);
        hosts.add(host);
        updateList();
    }
    
    private long getLongIp(InetAddress host){
        long ip;
        long workLong1, workLong2, workLong3, workLong4;
        Long ident;
        
        byte[] buff = host.getAddress();
        workLong1 = (long)buff[0] & 0x000000FF;
        workLong1 = workLong1 << 24;
        workLong2 = (long)buff[1] & 0x000000FF;
        workLong2 = workLong2 << 16;
        workLong3 = (long)buff[2] & 0x000000FF;
        workLong3 = workLong3 << 8;
        workLong4 = (long)buff[3] & 0x000000FF;
        ip = workLong1 | workLong2 | workLong3 | workLong4;
        
        return ip;
    }

    public void newFlow(Flow flow) {
        if(filter.allow(flow)){
            Host host;
            ModLong tLong = tmpLong;

            tLong.setLong(flow.getSourceIp());
            host = (Host)ipToHost.get(tLong);
            if(host != null){
                host.newFlowSource(flow);
            }

            tLong.setLong(flow.getDestinationIp());
            host = (Host)ipToHost.get(tLong);
            if(host != null){
                host.newFlowDestination(flow);
            }
        }
    }

    public void setFilter(Filter filter) {
    }

    public boolean allowFilters() {
        return false;
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

    public Filter getFilter() {
        return filter;
    }

    public ModularFlowProcessor getFlowPocessor() {
        return this;
    }

    public JComponent getJComponent() {
        return displayPanel;
    }

    public Object getProperty(String name) {
        return null;
    }

    public RefreshNotifier getRefreshNotifier() {
        return this;
    }

    public String getTitle() {
        return "Host View";
    }

    public boolean isConfigSeparate() {
        return true;
    }

    public boolean isControlSeparate() {
        return true;
    }

    public void reset() {
    }

    public void setProperty(String name, Object value) {
    }

    public void showLegend(boolean b) {
    }

    public void showTitle(boolean b) {
    }

    public void start() {
    }

    public void stop() {
    }

    public void updateData() {
        Iterator it = ipToHost.values().iterator();
        
        while(it.hasNext()){
            Host host = (Host)it.next();
            
            host.updateData();
        }
    }

    public void setStream(StreamDataSource sds) {
    }
}