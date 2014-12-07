package eunomia.module.common.proc.spammer;

import com.vivic.eunomia.sys.util.Util;
import eunomia.util.number.ModInteger;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MailServer implements Externalizable {
    private List currentCons;
    private List completedCons;
    private long ip;
    
    private ModInteger retriever;
    
    public MailServer(long ip) {
        currentCons = new ArrayList();
        completedCons = new ArrayList();
        this.ip = ip;
        
        retriever = new ModInteger();
    }
    
    public long getIp() {
        return ip;
    }
    
    public void updateInstance(long destIp, int srcPort, long startTime, long endTime) {
        MailInstance inst;
        
        if(endTime == -1) {
            // the connection is starting.
            retriever.setInt((int)destIp ^ srcPort);
            int index = currentCons.indexOf(retriever);
            
            if(index != -1) {
                // continued connection.
                inst = (MailInstance)currentCons.get(index);
            } else {
                inst = new MailInstance(destIp, srcPort, startTime);
                currentCons.add(inst);
            }
        } else {
            // try to find the existing connection.
            retriever.setInt((int)destIp ^ srcPort);
            int index = currentCons.indexOf(retriever);
            
            if(index != -1) {
                inst = (MailInstance)currentCons.get(index);
            } else {
                // ending connection we never started?
                inst = new MailInstance(destIp, srcPort, endTime);
            }
            
            inst.setCompleted(endTime);
            
            completedCons.add(inst);
        }
        
        System.out.println("Server " + Util.ipToString(ip) + " emailed " + Util.ipToString(inst.getDestinationIp()) + " from port " + inst.getSourcePort());
    }
    
    public int getTotalConnectionCount(long time, long interval) {
        // TODO: It's return all for now.
        return currentCons.size() + completedCons.size();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(ip);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ip = in.readLong();
    }
    
    public int hashCode() {
        return (int)(ip & 0xFFFFFFFFL);
    }
    
    public boolean equals(Object o) {
        if(o instanceof MailServer) {
            return ((MailServer)o).ip == ip;
        }
        
        return false;
    }
}