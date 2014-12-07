/*
 * HostDetail.java
 *
 * Created on April 22, 2006, 4:27 PM
 *
 */

package eunomia.plugin.gui.hostDetails;

import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.plugin.oth.hostDetails.conv.ConversationList;
import eunomia.util.Util;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HostDetail {
    private DetailedView dPanel;
    private ConversationList cList;
    private long hostIp;
    
    //host data
    private long startTime;
    private long totalBytes, inBytes, outBytes;
    private double outRate, inRate;
    private long[][] dataTable; //[0][] - IN, [1][] - OUT, [2][] - TOTAL
    
    public HostDetail(long ip, ConsoleReceptor receptor) {
        hostIp = ip;
        cList = new ConversationList();
        dataTable = new long[3][];
        for(int i = 0; i < 3; ++i){
            dataTable[i] = new long[NABFlow.NUM_TYPES + 1];
        }
        dPanel = new DetailedView(Util.getInetAddress(ip), this, dataTable);
        dPanel.setGlobalSettings(receptor.getGlobalSettings());
                
        // turns off gc
        cList.setGCTimeInterval(-1, 0);
    }
    
    public int getConversationCount(){
        return cList.convCount();
    }
    
    public long getHostIp(){
        return hostIp;
    }
    
    public void readIn(DataInputStream din) throws IOException {
        startTime = din.readLong();
        totalBytes = din.readLong();
        inBytes = din.readLong();
        outBytes = din.readLong();
        outRate = din.readDouble();
        inRate = din.readDouble();
        for (int i = 0; i < dataTable.length; i++) {
            long[] dTable = dataTable[i];
            for (int k = 0; k < dTable.length; k++) {
                dTable[k] = din.readLong();
            }
        }
        
        cList.readIn(din);
        
        dPanel.setConversationList(cList.getArray());
        dPanel.refresh();
    }

    public DetailedView getDetailedPanel() {
        return dPanel;
    }
    
    public String toString(){
        return dPanel.getAddress().toString();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getInBytes() {
        return inBytes;
    }

    public long getOutBytes() {
        return outBytes;
    }

    public double getOutRate() {
        return outRate;
    }

    public double getInRate() {
        return inRate;
    }
}