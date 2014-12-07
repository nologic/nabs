package eunomia.module.receptor.flow.DNSFlow;

import com.vivic.eunomia.module.flow.Flow;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import java.util.ArrayList;

public class DNSFlow implements Flow {
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
    private byte[] nameBuff;
    private String name;
    private char queryType;
    private char answerCount;
    private char authorityCount;
    private char additionalCount;
    //private ArrayList<DNSResponse> responses;
    private DNSFlowResponse[] answers;
    private DNSFlowResponse[] authorities;
    private DNSFlowResponse[] additionals;
    
    public DNSFlow() {
        //responses = new ArrayList<DNSResponse>();
        answers = new DNSFlowResponse[10];
        authorities = new DNSFlowResponse[10];
        additionals = new DNSFlowResponse[10];
        for (int i = 0; i < 10; ++i) {
            answers[i] = new DNSFlowResponse();
            authorities[i] = new DNSFlowResponse();
            additionals[i] = new DNSFlowResponse();
        }

        nameBuff = new byte[0x00FF];
    }

    /**
     * 
     * @return Unix time, as read by the flow. It it is generally the time on the server.
     */
    public long getTime() {
        return (long)((startTimeSeconds * 1000.0) + (startTimeMicroSeconds / 1000.0));
    }

    
    public int getSize() {
        return (int) bodyLength;
    }
    
    /**
     * Generic way for retrieving flow specific data. It depends on implementation but 
     * generally not efficient.
     */
    public Object getSpecificInfo(Object format) {
        // TODO
        return null;
    }
    
    /**
     * Internal network representation of the flow. This does not have to follow any 
     * standard. As long as the FlowModule maintains consistency on both sides.
     */
    public void writeToDataStream(DataOutputStream dout) throws IOException {
        ByteBuffer b = ByteBuffer.allocate(32 + bodyLength);
        
        b.order(ByteOrder.BIG_ENDIAN);
        
        b.put((byte) templateID);
        b.putChar(bodyLength);
        b.put((byte) protocol);
        b.putChar(sourcePort);
        b.putChar(destinationPort);
        b.putInt((int) sourceIP);
        b.putInt((int) destinationIP);
        b.putInt((int) startTimeSeconds);
        b.putInt((int) startTimeMicroSeconds);
        b.putInt((int) endTimeSeconds);
        b.putInt((int) endTimeMicroSeconds);
        
        b.putChar(queryFlags);
        b.putChar(responseFlags);
        b.putChar(queryType);
        b.putChar(answerCount);
        b.putChar(authorityCount);
        b.putChar(additionalCount);
        
        for (int i = 0; i < answerCount; ++i) {
            answers[i].writeToBuffer(b);
        }
        
        for (int i = 0; i < authorityCount; ++i) {
            authorities[i].writeToBuffer(b);
        }
        
        for (int i = 0; i < additionalCount; ++i) {
            additionals[i].writeToBuffer(b);
        }
        
        dout.write(b.array());
    }
    
    /**
     * This function is not used by this flow. Instead, readHeaderFromBuffer() and
     * readBodyFromBuffer() are used.
     * 
     * @param buffer unused
     */
    public void readFromByteBuffer(ByteBuffer buffer) {
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

    public int getSourcePort() {
        return (int) sourcePort;
    }

    public void setSourcePort(char sourcePort) {
        this.sourcePort = sourcePort;
    }

    public int getDestinationPort() {
        return (int) destinationPort;
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

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public char getQueryType() {
        return queryType;
    }
    
    public String getQueryTypeName() {
        if (queryType == TYPE_A) {
            return typeNames[0];
        } else if (queryType == TYPE_CNAME) {
            return typeNames[1];
        } else if (queryType == TYPE_PTR) {
            return typeNames[2];
        } else if (queryType == TYPE_MX) {
            return typeNames[3];
        } else if (queryType == TYPE_TXT) {
            return typeNames[4];
        } else if (queryType == TYPE_SOA) {
            return typeNames[5];
        }
        
        return "UNKNOWN";
    }
    
    public void setQueryType(char queryType) {
        this.queryType = queryType;
    }

    public char getAnswerCount() {
        return answerCount;
    }
    
    public void setAnswerCount(char answerCount) {
        this.answerCount = answerCount;
        if ((int) answerCount > answers.length) {
            DNSFlowResponse[] tmp = new DNSFlowResponse[(int) answerCount];
            
            System.arraycopy(answers, 0, tmp, 0, answers.length);
            for (int i = answers.length; i < tmp.length; ++i) {
                tmp[i] = new DNSFlowResponse();
            }
            
            answers = tmp;
        }
    }
    
    public DNSFlowResponse[] getAnswers() {
        return answers;
    }
    
    public char getAuthorityCount() {
        return authorityCount;
    }
    
    public void setAuthorityCount(char authorityCount) {
        this.authorityCount = authorityCount;
        if ((int) authorityCount > authorities.length) {
            DNSFlowResponse[] tmp = new DNSFlowResponse[(int) authorityCount];
            
            System.arraycopy(authorities, 0, tmp, 0, authorities.length);
            for (int i = authorities.length; i < tmp.length; ++i) {
                tmp[i] = new DNSFlowResponse();
            }
            
            authorities = tmp;
        }
    }
    
    public DNSFlowResponse[] getAuthorities() {
        return authorities;
    }
    
    public char getAdditionalCount() {
        return additionalCount;
    }
    
    public void setAdditionalCount(char additionalCount) {
        this.additionalCount = additionalCount;
        if ((int) additionalCount > additionals.length) {
            DNSFlowResponse[] tmp = new DNSFlowResponse[(int) additionalCount];
            
            System.arraycopy(additionals, 0, tmp, 0, additionals.length);
            for (int i = additionals.length; i < tmp.length; ++i) {
                tmp[i] = new DNSFlowResponse();
            }
            
            additionals = tmp;
        }
    }
    
    public DNSFlowResponse[] getAdditionals() {
        return additionals;
    }
    
    public void readHeaderFromBuffer(ByteBuffer buff) {
        ByteOrder order = buff.order();
        
        buff.order(ByteOrder.BIG_ENDIAN);
        
        // fetch the header from the byte buffer and store it in the flow
        templateID = (short) (buff.get() & 0x00FF);
        bodyLength = buff.getChar();
        protocol = (short) (buff.get() & 0x00FF);
        sourcePort = buff.getChar();
        destinationPort = buff.getChar();
        sourceIP = (long) (buff.getInt() & 0xFFFFFFFFL);
        destinationIP = (long) (buff.getInt() & 0xFFFFFFFFL);
        startTimeSeconds = (long) (buff.getInt() & 0xFFFFFFFFL);
        startTimeMicroSeconds = (long) (buff.getInt() & 0xFFFFFFFFL);
        endTimeSeconds = (long) (buff.getInt() & 0xFFFFFFFFL);
        endTimeMicroSeconds = (long) (buff.getInt() & 0xFFFFFFFFL);
        
        buff.order(order);
    }
    
    public void readBodyFromBuffer(ByteBuffer buff) throws UnsupportedEncodingException {      
        ByteOrder order = buff.order();
        
        buff.order(ByteOrder.BIG_ENDIAN);
        
        // fetch body section from the byte buffer and store it in the flow
        queryFlags = buff.getChar();
        responseFlags = buff.getChar();
        nameLength = (short) (buff.get() & 0x00FF);
        buff.get(nameBuff, 0, (int) nameLength);
        name = new String(nameBuff, 0, nameLength, "US-ASCII");
        queryType = buff.getChar();
        answerCount = buff.getChar();
        authorityCount = buff.getChar();
        additionalCount = buff.getChar();
        
        // fetch answers
        //try {
        answers = readResponsesFromBuffer(answers, buff, answerCount);
        /*
        } catch (IndexOutOfBoundsException e) {
            System.err.println("We hit the exception...");
            e.printStackTrace();
            System.err.println("Some info:");
            System.err.println("    Server IP:        " + destinationIP);
            System.err.println("    Query name:       " + name);
            System.err.println("    Query type:       " + (int) queryType);
            System.err.println("    Answer count:     " + (int) answerCount);
            System.err.println("    Authority count:  " + (int) authorityCount);
            System.err.println("    Additional count: " + (int) additionalCount);
        }
        */
        
        // fetch authorities
        authorities = readResponsesFromBuffer(authorities, buff, authorityCount);
        
        // fetch additionals
        additionals = readResponsesFromBuffer(additionals, buff, additionalCount);
        
        // reset buff's byte order
        buff.order(order);
    }
    
    public DNSFlowResponse[] readResponsesFromBuffer(DNSFlowResponse[] responses, ByteBuffer buff, char count) throws UnsupportedEncodingException {
        if (count == 0x0000) {
            return responses;
        }
        
        if ((int) count > responses.length) {
            DNSFlowResponse[] tmp = new DNSFlowResponse[(int) count];
            
            System.arraycopy(responses, 0, tmp, 0, responses.length);
            for (int i = responses.length; i < tmp.length; ++i) {
                tmp[i] = new DNSFlowResponse();
            }
            
            responses = tmp;
        }

        for (int i = 0; i < (int) count; ++i) {
            responses[i].readFromBuffer(buff);
        }
        
        return responses;
    }
}