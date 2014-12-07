package eunomia.module.receptor.libb.imsCore.dns;

import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import eunomia.module.receptor.libb.imsCore.StoreEnvironment;
import eunomia.module.receptor.libb.imsCore.bind.ByteUtils;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Justin Stallard
 */
public class DNSFlowRecord extends EnvironmentEntry {
    private DNSFlowKey key;
    //private byte[] record;
    
    public static final short PROTOCOL_TCP = 6;
    public static final short PROTOCOL_UDP = 17;
    
    public static final short NUM_TYPES  = 4;
    
    // TYPE values
    public static final char TYPE_A      = 0x0001;
    /*
    public static final char TYPE_NS     = 0x0002;
    public static final char TYPE_MD     = 0x0003;
    public static final char TYPE_MF     = 0x0004;
    */
    public static final char TYPE_CNAME  = 0x0005;
    public static final char TYPE_SOA    = 0x0006;
    /*
    public static final char TYPE_MB     = 0x0007;
    public static final char TYPE_MG     = 0x0008;
    public static final char TYPE_MR     = 0x0009;
    public static final char TYPE_NULL   = 0x000A;
    public static final char TYPE_WKS    = 0x000B;
    */
    public static final char TYPE_PTR    = 0x000C;
    /*
    public static final char TYPE_HINFO  = 0x000D;
    public static final char TYPE_MINFO  = 0x000E;
    */
    public static final char TYPE_MX     = 0x000F;
    public static final char TYPE_TXT    = 0x0010;
    /*
    
    // QTYPE values
    public static final char TYPE_AXFR   = 0x00FC;
    public static final char TYPE_MAILB  = 0x00FD;
    public static final char TYPE_MAILA  = 0x00FE;
    public static final char TYPE_ALL    = 0x00FF;
    */
    
    public static final String[] typeNames = {"A", "CNAME", "PTR", "MX", "TXT", "SOA"};
    
    // header fields
    
    private short templateID;
    private short protocol;
    
    // using char as unsigned short
    private char bodyLength;
    private char sourcePort;
    private char destinationPort;
    
    private long sourceIP;
    private long destinationIP;
    private long startTimeSeconds;
    private long startTimeMicroSeconds;
    private long endTimeSeconds;
    private long endTimeMicroSeconds;
    
    // DNS related fields
    // using char as unsigned short
    private char queryFlags;
    private char responseFlags;
    private short nameLength;
    private String name;
    private char queryType;
    private char answerCount;
    private char authorityCount;
    private char additionalCount;
    private DNSResponse[] answers;
    private DNSResponse[] authorities;
    private DNSResponse[] additionals;

    public DNSFlowRecord(StoreEnvironment env) {
        super(env);
        
        key = new DNSFlowKey();
    }
    
    public void setRecordData(byte[] data) {
        /*
        record = new byte[data.length];
        System.arraycopy(data, 0, record, 0, data.length);
        */
        unserialize(data, 0);
    }
    
    @Override
    public EnvironmentKey getKey() {
        return key;
    }

    @Override
    public EnvironmentEntry clone() {
        DNSFlowRecord rec = new DNSFlowRecord(env);
        
        //System.arraycopy(record, 0, rec, 0, record.length);
        rec.key = (DNSFlowKey) this.key.clone();
        
        // Header
        rec.templateID = this.templateID;
        rec.bodyLength = this.bodyLength;
        rec.protocol = this.protocol;
        rec.sourcePort = this.sourcePort;
        rec.destinationPort = this.destinationPort;
        rec.sourceIP = this.sourceIP;
        rec.destinationIP = this.destinationIP;
        rec.startTimeSeconds = this.startTimeSeconds;
        rec.startTimeMicroSeconds = this.startTimeMicroSeconds;
        rec.endTimeSeconds = this.endTimeSeconds;
        rec.endTimeMicroSeconds = this.endTimeMicroSeconds;
        
        // Query section
        rec.queryFlags = this.queryFlags;
        rec.responseFlags = this.responseFlags;
        rec.nameLength = this.nameLength;
        rec.name = this.name;
        rec.queryType = this.queryType;
        rec.answerCount = this.answerCount;
        rec.authorityCount = this.authorityCount;
        rec.additionalCount = this.additionalCount;
        
        // Answers
        rec.answers = new DNSResponse[answerCount];
        for (int i = 0; i < (int) answerCount; ++i) {
            rec.answers[i] = this.answers[i].clone();
        }
        
        // Authorities
        rec.authorities = new DNSResponse[authorityCount];
        for (int i = 0; i < (int) authorityCount; ++i) {
            rec.authorities[i] = this.authorities[i].clone();
        }
        
        // Additionals
        rec.additionals = new DNSResponse[additionalCount];
        for (int i = 0; i < (int) additionalCount; ++i) {
            rec.additionals[i] = this.additionals[i].clone();
        }
        
        return rec;
    }
    
    public long getTimeSlice() {
        //return ((long) ByteUtils.bytesToInt(record, 24) &  0xFFFFFFFFL) >> DNS.TIME_SHIFT;
        return endTimeSeconds >> DNS.TIME_SHIFT;
    }

    public int getByteSize() {
        //return record.length + key.getByteSize();
        int ret =  32            // header
                 + 2             // query flags
                 + 2             // response flags
                 + 1             // name length
                 + nameLength    // name
                 + 2             // query type
                 + 2             // answerCount
                 + 2             // additionalCount
                 + 2;            // authorityCount
        
        // answers
        for (int i = 0; i < (int) answerCount; ++i) {
            ret += answers[i].getByteSize();
        }
        
        // additionals
        for (int i = 0; i < (int) additionalCount; ++i) {
            ret += additionals[i].getByteSize();
        }
        
        // authorities
        for (int i = 0; i < (int) authorityCount; ++i) {
            ret += authorities[i].getByteSize();
        }
        
        return ret;
    }

    /*
    public void serialize(byte[] arr, int offset) {
        System.arraycopy(record, 0, arr, offset, record.length);
        offset += record.length;
        key.serialize(arr, offset);
        offset += key.getByteSize();
    }
    */
    
    public void serialize(byte[] arr, int offset) {
        // Header
        arr[offset] = (byte) (templateID & 0xFF);
        ++offset;
        offset += ByteUtils.charToBytes(arr, offset, bodyLength);
        arr[offset] = (byte) (protocol & 0xFF);
        ++offset;
        offset += ByteUtils.charToBytes(arr, offset, sourcePort);
        offset += ByteUtils.charToBytes(arr, offset, destinationPort);
        offset += ByteUtils.intToBytes(arr, offset, (int) (sourceIP & 0xFFFFFFFFL));
        offset += ByteUtils.intToBytes(arr, offset, (int) (destinationIP & 0xFFFFFFFFL));
        offset += ByteUtils.intToBytes(arr, offset, (int) (startTimeSeconds & 0xFFFFFFFFL));
        offset += ByteUtils.intToBytes(arr, offset, (int) (startTimeMicroSeconds & 0xFFFFFFFFL));
        offset += ByteUtils.intToBytes(arr, offset, (int) (endTimeSeconds & 0xFFFFFFFFL));
        offset += ByteUtils.intToBytes(arr, offset, (int) (endTimeMicroSeconds & 0xFFFFFFFFL));
        
        // Query section
        offset += ByteUtils.charToBytes(arr, offset, queryFlags);
        offset += ByteUtils.charToBytes(arr, offset, responseFlags);
        arr[offset] = (byte) nameLength;
        ++offset;
        try {
            System.arraycopy(name.getBytes("US-ASCII"), 0, arr, offset, name.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        offset += nameLength;
        offset += ByteUtils.charToBytes(arr, offset, queryType);
        offset += ByteUtils.charToBytes(arr, offset, answerCount);
        offset += ByteUtils.charToBytes(arr, offset, authorityCount);
        offset += ByteUtils.charToBytes(arr, offset, additionalCount);
        
        // Answers
        for (int i = 0; i < (int) answerCount; ++i) {
            answers[i].serialize(arr, offset);
            offset += answers[i].getByteSize();
        }
        
        // Authorities
        for (int i = 0; i < (int) authorityCount; ++i) {
            authorities[i].serialize(arr, offset);
            offset += authorities[i].getByteSize();
        }
        
        // Additionals
        for (int i = 0; i < (int) additionalCount; ++i) {
            additionals[i].serialize(arr, offset);
            offset += additionals[i].getByteSize();
        }
    }

    /*
    public void unserialize(byte[] arr, int offset) {
        record = new byte[arr.length - offset];
        System.arraycopy(arr, offset, record, 0, arr.length - offset);
        offset += arr.length - offset;
        key.unserialize(arr, offset);
        offset += key.getByteSize();
    }
    */
    public void unserialize(byte[] arr, int offset) {
        // Header
        templateID = (short) (arr[offset] & 0xFF);
        ++offset;
        bodyLength = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        protocol = (short) (arr[offset] & 0xFF);
        ++offset;
        sourcePort = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        destinationPort = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        sourceIP = (long) (ByteUtils.bytesToInt(arr, offset) & 0xFFFFFFFFL);
        offset += 4;
        destinationIP = (long) (ByteUtils.bytesToInt(arr, offset) & 0xFFFFFFFFL);
        offset += 4;
        startTimeSeconds = (long) (ByteUtils.bytesToInt(arr, offset) & 0xFFFFFFFFL);
        offset += 4;
        startTimeMicroSeconds = (long) (ByteUtils.bytesToInt(arr, offset) & 0xFFFFFFFFL);
        offset += 4;
        endTimeSeconds = (long) (ByteUtils.bytesToInt(arr, offset) & 0xFFFFFFFFL);
        offset += 4;
        endTimeMicroSeconds = (long) (ByteUtils.bytesToInt(arr, offset) & 0xFFFFFFFFL);
        offset += 4;
        
        // Query section
        queryFlags = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        responseFlags = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        nameLength = (short) (arr[offset] & 0xFF);
        ++offset;
        try {
            name = new String(arr, offset, nameLength, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        offset += nameLength;
        queryType = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        answerCount = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        authorityCount = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        additionalCount = ByteUtils.bytesToChar(arr, offset);
        offset += 2;
        
        // Answers
        for (int i = 0; i < (int) answerCount; ++i) {
            answers[i].unserialize(arr, offset);
            offset += answers[i].getByteSize();
        }
        
        // Authorities
        for (int i = 0; i < (int) authorityCount; ++i) {
            authorities[i].unserialize(arr, offset);
            offset += authorities[i].getByteSize();
        }
        
        // Additionals
        for (int i = 0; i < (int) additionalCount; ++i) {
            additionals[i].unserialize(arr, offset);
            offset += authorities[i].getByteSize();
        }
    }

    public short getTemplateID() {
        return templateID;
    }

    public void setTemplateID(short templateID) {
        this.templateID = templateID;
    }

    public short getProtocol() {
        return protocol;
    }

    public void setProtocol(short protocol) {
        this.protocol = protocol;
    }

    public char getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(char bodyLength) {
        this.bodyLength = bodyLength;
    }

    public char getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(char sourcePort) {
        this.sourcePort = sourcePort;
    }

    public char getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(char destinationPort) {
        this.destinationPort = destinationPort;
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

    public long getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(long startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public long getStartTimeMicroSeconds() {
        return startTimeMicroSeconds;
    }

    public void setStartTimeMicroSeconds(long startTimeMicroSeconds) {
        this.startTimeMicroSeconds = startTimeMicroSeconds;
    }

    public long getEndTimeSeconds() {
        return endTimeSeconds;
    }

    public void setEndTimeSeconds(long endTimeSeconds) {
        this.endTimeSeconds = endTimeSeconds;
    }

    public long getEndTimeMicroSeconds() {
        return endTimeMicroSeconds;
    }

    public void setEndTimeMicroSeconds(long endTimeMicroSeconds) {
        this.endTimeMicroSeconds = endTimeMicroSeconds;
    }

    public char getQueryFlags() {
        return queryFlags;
    }

    public void setQueryFlags(char queryFlags) {
        this.queryFlags = queryFlags;
    }

    public char getResponseFlags() {
        return responseFlags;
    }

    public void setResponseFlags(char responseFlags) {
        this.responseFlags = responseFlags;
    }

    public short getNameLength() {
        return nameLength;
    }

    public void setNameLength(short nameLength) {
        this.nameLength = nameLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameLength = (short) (name.length() & 0xFF);
    }

    public char getQueryType() {
        return queryType;
    }

    public void setQueryType(char queryType) {
        this.queryType = queryType;
    }

    public char getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(char answerCount) {
        this.answerCount = answerCount;
    }

    public char getAuthorityCount() {
        return authorityCount;
    }

    public void setAuthorityCount(char authorityCount) {
        this.authorityCount = authorityCount;
    }

    public char getAdditionalCount() {
        return additionalCount;
    }

    public void setAdditionalCount(char additionalCount) {
        this.additionalCount = additionalCount;
    }

    public DNSResponse[] getAnswers() {
        return answers;
    }

    public void setAnswers(DNSResponse[] answers) {
        this.answers = answers;
    }

    public DNSResponse[] getAuthorities() {
        return authorities;
    }

    public void setAuthorities(DNSResponse[] authorities) {
        this.authorities = authorities;
    }

    public DNSResponse[] getAdditionals() {
        return additionals;
    }

    public void setAdditionals(DNSResponse[] additionals) {
        this.additionals = additionals;
    }
    
    public static class DNSResponse {
        private String name;
        private char responseType;
        private int resourceDataLength;
        private byte[] resourceData;
        private int ttl;

        public DNSResponse() {
            resourceData = new byte[0xFFFF];
        }
        
        @Override
        public DNSResponse clone() {
            DNSResponse theClone = new DNSResponse();
            
            theClone.name = this.name;
            theClone.responseType = this.responseType;
            theClone.resourceDataLength = this.resourceDataLength;
            System.arraycopy(this.resourceData, 0, theClone.resourceData, 0, resourceDataLength);
            theClone.ttl = this.ttl;
            
            return theClone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public char getResponseType() {
            return responseType;
        }

        public void setResponseType(char responseType) {
            this.responseType = responseType;
        }

        public long getResourceDataIP() {
            long ip = 0;

            ip |= (long) (resourceData[0] & 0xFFL);
            for (int i = 1; i < 4; ++i) {
                ip <<= 8;
                ip |= (long) (resourceData[i] & 0xFFL);
            }

            return ip;
        }

        public String getResourceDataName() {
            try {
                return new String(resourceData, 0, (int) resourceDataLength, "US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DNSResponse.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        public int getResourceDataMXPref() {
            int ret = 0;
            ret |= resourceData[0];
            ret <<= 8;
            ret |= resourceData[1];
            return ret;
        }

        public String getResourceDataMXName() {
            try {
                return new String(resourceData, 2, (int) resourceDataLength - 2, "US-ASCII");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        public byte[] getResourceData() {
            return resourceData;
        }

        public int getResourceDataLength() {
            return resourceDataLength;
        }

        public void setResourceData(byte[] resourceData, int length) {
            System.arraycopy(resourceData, 0, this.resourceData, 0, length);
            resourceDataLength = length;
        }

        public int getTTL() {
            return ttl;
        }

        public void setTTL(int ttl) {
            this.ttl = ttl;
        }

        // deprecated. do not use
        public void readFromBuffer(ByteBuffer buff) throws UnsupportedEncodingException {
            ByteOrder order = buff.order();

            buff.order(ByteOrder.BIG_ENDIAN);

            resourceDataLength = buff.get() & 0x00FF;

            buff.get(resourceData, 0, resourceDataLength);
            name = new String(resourceData, 0, resourceDataLength, "US-ASCII");
            responseType = buff.getChar();

            // TODO (maybe) don't store resource data in a byte array
            resourceDataLength = buff.getShort() & 0x0000FFFF;
            buff.get(resourceData, 0, resourceDataLength);
            ttl = buff.getInt();

            buff.order(order);
        }

        // deprecated. do not use
        public void writeToBuffer(ByteBuffer buff) {
            try {
                buff.put((byte) name.length());
                buff.put(name.getBytes("US-ASCII"));
                buff.putChar(responseType);
                buff.put((byte) resourceDataLength);
                buff.put(resourceData, 0, resourceDataLength);
                buff.putInt(ttl);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DNSResponse.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public int getByteSize() {
            return   1                      // name length
                   + name.length()          // name
                   + 2                      // response type
                   + 2                      // resource data length
                   + resourceDataLength     // resource data
                   + 4;                     // ttl
        }
        
        public void serialize(byte[] arr, int offset) {
            arr[offset] = (byte) name.length();
            ++offset;
            try {
                System.arraycopy(name.getBytes("US-ASCII"), 0, arr, offset, name.length());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            offset += name.length();
            offset += ByteUtils.charToBytes(arr, offset, responseType);
            offset += ByteUtils.charToBytes(arr, offset, (char) resourceDataLength);
            System.arraycopy(resourceData, 0, arr, offset, resourceDataLength);
            offset += resourceDataLength;
            offset += ByteUtils.intToBytes(arr, offset, ttl);
        }
        
        public void unserialize(byte[] arr, int offset) {
            resourceDataLength = (int) (arr[offset] & 0xFF);
            ++offset;
            try {
                name = new String(arr, offset, resourceDataLength, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            offset += resourceDataLength;
            responseType = ByteUtils.bytesToChar(arr, offset);
            offset += 2;
            resourceDataLength = (int) (ByteUtils.bytesToChar(arr, offset) & 0xFFFF);
            offset += 2;
            System.arraycopy(arr, offset, resourceData, 0, resourceDataLength);
            offset += resourceDataLength;
            ttl = ByteUtils.bytesToInt(arr, offset);
            offset += 4;
        }
    }
}