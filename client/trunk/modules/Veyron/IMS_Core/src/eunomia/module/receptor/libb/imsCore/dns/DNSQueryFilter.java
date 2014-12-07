package eunomia.module.receptor.libb.imsCore.dns;

import eunomia.module.receptor.libb.imsCore.iterators.IteratorFilter;

/**
 *
 * @author Justin Stallard
 */
public class DNSQueryFilter implements IteratorFilter {
    
    private long queryTimeIntervalStartSeconds;
    private long queryTimeIntervalStartMicroSeconds;
    private long queryTimeIntervalEndSeconds;
    private long queryTimeIntervalEndMicroSeconds;
    
    private short protocol;
    private long clientIP;
    private long clientMask;
    private char clientPort;
    private long serverIP;
    private long serverMask;
    private char serverPort;
    
    // TODO???
    //private char queryFlags;
    //private char responseFlags;
    //private short nameLength;
    
    private String name;
    private char queryType;

    // returns true if every field that is set in the filter matches the 
    // corresponding field in in the DNSFlowRecord being tested
    public boolean allow(Object o) {
        if (!(o instanceof DNSFlowRecord)) {
            return false;
        }
        
        DNSFlowRecord r = (DNSFlowRecord) o;
        
        if (!(queryTimeIntervalStartSeconds == 0 && queryTimeIntervalStartMicroSeconds == 0) &&
             (queryTimeIntervalStartSeconds > r.getStartTimeSeconds() ||
                (queryTimeIntervalStartSeconds == r.getStartTimeSeconds() && queryTimeIntervalStartMicroSeconds > r.getStartTimeMicroSeconds())))
        {
            return false;
        }
        
        if (!(queryTimeIntervalEndSeconds == 0 && queryTimeIntervalStartMicroSeconds == 0) &&
             (queryTimeIntervalEndSeconds < r.getStartTimeSeconds() ||
                (queryTimeIntervalEndSeconds == r.getStartTimeSeconds() && queryTimeIntervalEndMicroSeconds <= r.getStartTimeMicroSeconds())))
        {
            return false;
        }
        
        if (protocol != 0 && protocol != r.getProtocol()) {
            return false;
        }
        
        if (clientIP != 0 && (r.getSourceIP() & clientMask) != clientIP) {
            return false;
        }
        
        if (clientPort != 0 && clientPort != r.getSourcePort()) {
            return false;
        }
        
        if (serverIP != 0 && (r.getDestinationIP() & serverMask) != serverIP) {
            return false;
        }
        
        if (serverPort != 0 && serverPort != r.getDestinationPort()) {
            return false;
        }
        
        if (name != null && !r.getName().equals(name)) {
            return false;
        }
        
        if (queryType != 0 && queryType != r.getQueryType()) {
            return false;
        }
        
        return true;
    }
    
    public void setQueryTimeIntervalStart(long seconds, long microSeconds) throws Exception {
        if ((queryTimeIntervalEndSeconds == 0 && queryTimeIntervalEndMicroSeconds == 0) ||
            (queryTimeIntervalEndSeconds > seconds) ||
            (queryTimeIntervalEndSeconds == seconds && queryTimeIntervalEndMicroSeconds > microSeconds))
        {
            queryTimeIntervalStartSeconds = seconds;
            queryTimeIntervalStartMicroSeconds = microSeconds;
            return;
        }
        
        throw new Exception("Cannot set start of interval later than end of interval.");
    }
    
    public void setQueryTimeIntervalEnd(long seconds, long microSeconds) throws Exception {
        if ((queryTimeIntervalStartSeconds == 0 && queryTimeIntervalStartMicroSeconds == 0) ||
            (queryTimeIntervalStartSeconds < seconds) ||
            (queryTimeIntervalStartSeconds == seconds && queryTimeIntervalStartMicroSeconds < microSeconds))
        {
            queryTimeIntervalStartSeconds = seconds;
            queryTimeIntervalStartMicroSeconds = microSeconds;
            return;
        }
        
        throw new Exception("Cannot set end of interval earlier than start of interval.");
    }
    
    public void setProtocol(short protocol) {
        this.protocol = protocol;
    }
    
    public void setClientIP(long clientIP) {
        this.clientIP = clientIP;
        this.clientMask = 0xFFFFFFFFL;
    }

    public void setClientNetwork(long clientNetwork, long clientMask) throws Exception {
        if ((clientNetwork & clientMask) != clientNetwork) {
            throw new Exception("Invalid network/mask pair.");
        }
        
        this.clientIP = clientNetwork;
        this.clientMask = clientMask;
    }
    
    public void setClientPort(char clientPort) {
        this.clientPort = clientPort;
    }
    
    public void setServerIP(long serverIP) {
        this.serverIP = serverIP;
        this.serverMask = 0xFFFFFFFFL;
    }
    
    public void setServerNetwork(long serverNetwork, long serverMask) throws Exception {
        if ((serverNetwork & serverMask) != serverNetwork) {
            throw new Exception("Invalid network/mask pair.");
        }
        
        this.serverIP = serverNetwork;
        this.serverMask = serverMask;
    }
    
    public void setServerPort(char serverPort) {
        this.serverPort = serverPort;
    }
    
    // TODO queryFlags, responseFlags, nameLength???
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setQueryType(char queryType) {
        this.queryType = queryType;
    }
}