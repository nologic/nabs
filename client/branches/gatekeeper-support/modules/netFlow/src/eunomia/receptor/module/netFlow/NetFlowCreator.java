/*
 * NetFlowCreator.java
 *
 * Created on August 23, 2006, 9:25 PM
 *
 */

package eunomia.receptor.module.netFlow;

import com.vivic.eunomia.module.Flow;
import com.vivic.eunomia.module.receptor.FlowCreator;
import eunomia.util.number.ModInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetFlowCreator implements FlowCreator {
    public static final int TEMPLATE_FLOWSET = 0;
    public static final int OPTION_FLOWSET = 1;
    
    private HashMap idToTemplate;
    private ModInteger retriever;
    private NetFlow flow;
    
    //current packet
    private boolean gotHeader;
    private PacketHeader header;
    private int nextLength;
    private int setsLeft;
    
    //current flowset
    private int flowSetID;
    private int length;

    public NetFlowCreator() {
        header = new PacketHeader();
        retriever = new ModInteger();
        idToTemplate = new HashMap();
        setsLeft = 0;
        flow = new NetFlow();
    }

    public int getNextFlowMinSize() {
        if(!gotHeader){
            return header.getSize();
        }
        
        return nextLength;
    }

    public int getBufferSize() {
        return 65536;
    }
    
    public NetFlowTemplate getNetFlowTemplate(int id){
        retriever.setInt(id);
        NetFlowTemplate template = (NetFlowTemplate)idToTemplate.get(retriever);
        
        if(template == null){
            template = new NetFlowTemplate(id);
            idToTemplate.put(template, template);
        }
        
        return template;
    }

    public Flow processBuffer(ByteBuffer buff) {
        if(!gotHeader){
            header.parseHeader(buff);
            setsLeft = header.getCount();
            nextLength = 4;
            gotHeader = true;
        }
        
        if(nextLength == 4){ // read next FlowSet header;
            if(buff.remaining() >= nextLength){
                flowSetID = buff.getShort() & 0xFFFF;
                length = buff.getShort() & 0xFFFF;
                length -= 4; // not to count the previous 2 fields;
                nextLength = length;
            }
        }
        
        if(buff.remaining() >= nextLength){
            --setsLeft;
            //parse flowset;
            NetFlowTemplate template = null;
            if(flowSetID > 255){
                //parse data flowset
                template = getNetFlowTemplate(flowSetID);
                template.parseData(buff);
                //set the data to the flow interface.
                flow.setHeader(header);
                flow.setTemplate(template);
            } else {
                switch(flowSetID){
                    case TEMPLATE_FLOWSET: {
                        // template;
                        int templateID = buff.getShort() & 0xFFFF;
                        template = getNetFlowTemplate(flowSetID);
                        template.updateTemplate(buff);
                        
                        //consume bytes.
                        int extra = nextLength - template.getDataSetLength();
                        while(--extra != -1){
                            buff.get();
                        }
                        break;
                    }
                    case OPTION_FLOWSET: {
                        // option parsing, no parsing yet. Just consuming the bytes.
                        int l = nextLength;
                        while(--l != -1){
                            buff.get();
                        }
                        break;
                    }
                }
            }
            
            if(setsLeft == 0){
                gotHeader = false;
            } else {
                nextLength = 4;
            }
            
            return flow;
        }
        
        return null;
    }
}