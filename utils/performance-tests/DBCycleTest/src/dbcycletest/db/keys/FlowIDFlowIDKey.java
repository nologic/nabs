package dbcycletest.db.keys;

/**
 *
 * @author Justin Stallard
 */
public class FlowIDFlowIDKey {
    private byte protocol;
    private long sourceIP;
    private long destinationIP;
    private int sourcePort;
    private int destinationPort;
    
    public FlowIDFlowIDKey() {
        
    }
    
    public boolean equals(FlowIDFlowIDKey key) {
        return (key.protocol == protocol &&
                key.sourceIP == sourceIP &&
                key.destinationIP == destinationIP &&
                key.sourcePort == sourcePort &&
                key.destinationPort == destinationPort);
    }

    public byte getProtocol() {
        return protocol;
    }

    public void setProtocol(byte protocol) {
        this.protocol = protocol;
    }

    public long getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(long sourceIP) {
        this.sourceIP = sourceIP;
    }

    public long getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(long destinationIP) {
        this.destinationIP = destinationIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }
    
    public FlowIDFlowIDKey getReverseKey() {
        FlowIDFlowIDKey reverse = new FlowIDFlowIDKey();
        
        reverse.protocol = protocol;
        reverse.sourceIP = destinationIP;
        reverse.destinationIP = sourceIP;
        reverse.sourcePort = destinationPort;
        reverse.destinationPort = sourcePort;
        
        return reverse;
    }
}