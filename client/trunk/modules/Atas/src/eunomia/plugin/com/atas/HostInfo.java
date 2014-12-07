/*
 * HostInfo.java
 *
 * Created on January 17, 2007, 12:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.com.atas;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import com.vivic.eunomia.sys.util.Util;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author kulesh
 * This class will carry the necessary information needed by the GUI to display
 * various information about a host, such as IP addresses, port numbers, etc.
 * For now GUI shall just call toString() to display information about a host.
 *
 */
public class HostInfo  implements Externalizable{
    
    private long ip;
    private long packets;
    private long bytes;
    private long lastSeen;
    
    /** Creates a new instance of HostInfo */
    public HostInfo() {
    }
    
    public HostInfo(long ip) {
        setIp(ip);
    }
    
    public HostInfo(HostInfo hi) {
        setIp(hi.ip);
        setPackets(hi.packets);
        setBytes(hi.bytes);
        setLastSeen(hi.lastSeen);
    }
    
    public boolean equals(HostInfo other) {
        return((this.ip == other.ip));
    }

	public boolean equals(Object o){
		return	equals((HostInfo)o);
	}
    
    public String toString(){
        StringBuilder tmp= new StringBuilder();
        Util.ipToString(tmp,ip);
		tmp.append(": Packets= " + packets + " Bytes= " + bytes + "Last Seen= " + lastSeen);
        
        return tmp.toString();
    }
    
    public long getIp() {
        return ip;
    }
    
    public void setIp(long ip) {
        this.ip = ip;
    }
    
    public long getPackets() {
        return packets;
    }
    
    public void setPackets(long packets) {
        this.packets = packets;
    }

    public void incrementPackets(long packets){
        this.packets += packets;
    }
    
    public long getBytes() {
        return bytes;
    }
    
    public void setBytes(long bytes) {
        this.bytes = bytes;
    }
    
    public void incrementBytes(long bytes){
        this.bytes += bytes;
    }
    
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public void updateStats(HostInfo other){
        if(this.ip != other.ip) return;
        
        setPackets(other.packets);
        setBytes(other.bytes);
        setLastSeen(other.lastSeen);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(ip);
        out.writeLong(packets);
        out.writeLong(bytes);
        out.writeLong(lastSeen);
        
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ip= in.readLong();
        packets= in.readLong();
        bytes= in.readLong();
        lastSeen= in.readLong();
    }
}
