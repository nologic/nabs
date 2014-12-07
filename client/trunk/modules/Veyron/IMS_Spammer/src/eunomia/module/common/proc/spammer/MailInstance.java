package eunomia.module.common.proc.spammer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class MailInstance {
    private long destIp;
    private int srcPort;
    
    private long initiated;
    private long completed;
    
    public MailInstance(long destIp, int srcPort, long init) {
        this.destIp = destIp;
        this.srcPort = srcPort;
        this.initiated = init;
    }
    
    public boolean equals(Object o) {
        MailInstance inst = (MailInstance)o;
        
        return inst.destIp == destIp && inst.srcPort == srcPort;
    }
    
    public int hashCode() {
        return (int)destIp ^ srcPort;
    }

    public long getInitiated() {
        return initiated;
    }

    public void setInitiated(long initiated) {
        this.initiated = initiated;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getDestinationIp() {
        return destIp;
    }

    public int getSourcePort() {
        return srcPort;
    }
}