package dbcycletest;

import java.util.Random;

/**
 *
 * @author Justin Stallard
 */
public class FlowID {
    private int key;
    
    private byte protocol;
    private long sourceIP;
    private long destinationIP;
    private int sourcePort;
    private int destinationPort;
    private long startTimeSeconds;
    private long startTimeMicroseconds;
    private long endTimeSeconds;
    private long endTimeMicroseconds;

    private static Random rand;
    
    public FlowID() {
        if (rand == null) {
            rand = new Random(1);
        }
    }
    
    public void makeRandom() {
        startTimeSeconds = ((rand.nextInt() & 0xFFFFFFFFL) % 86400) + 1208285484L;
        endTimeSeconds = startTimeSeconds + ((rand.nextInt() & 0xFFFFFFFFL) % 3600);
        startTimeMicroseconds = (rand.nextInt() & 0xFFFFFFFFL) % 1000000L;
        endTimeMicroseconds = (rand.nextInt() & 0xFFFFFFFFL) % 1000000L;
        
        sourceIP = ((rand.nextInt() & 0xFFFFFFFFL) % 3000) + 0x12088770L;
        destinationIP = rand.nextInt() & 0xFFFFFFFFL;
        
        sourcePort = (int) (((rand.nextInt() & 0xFFFFFFFFL) % 65536) + 1024);
        destinationPort = (int) (((rand.nextInt() & 0xFFFFFFFFL) % 1024) + 1);
        
        protocol = (byte) ((rand.nextInt() & 0xFFFFFFFFL) % 5);
    }
    
    public int getKey() {
        return key;
    }
    
    public void setKey(int key) {
        this.key = key;
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

    public long getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(long startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public long getStartTimeMicroseconds() {
        return startTimeMicroseconds;
    }

    public void setStartTimeMicroseconds(long startTimeMicroseconds) {
        this.startTimeMicroseconds = startTimeMicroseconds;
    }

    public long getEndTimeSeconds() {
        return endTimeSeconds;
    }

    public void setEndTimeSeconds(long endTimeSeconds) {
        this.endTimeSeconds = endTimeSeconds;
    }

    public long getEndTimeMicroseconds() {
        return endTimeMicroseconds;
    }

    public void setEndTimeMicroseconds(long endTimeMicroseconds) {
        this.endTimeMicroseconds = endTimeMicroseconds;
    }
}