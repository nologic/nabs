/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.dnsCollect;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.module.receptor.flow.DNSFlow.DNSFlow;
import eunomia.module.receptor.flow.DNSFlow.DNSFlowResponse;
import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import eunomia.module.receptor.libb.imsCore.dns.DNS;
import eunomia.module.receptor.libb.imsCore.dns.DNSFlowRecord;
import eunomia.module.receptor.libb.imsCore.util.ComponentRegistry;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author justin
 */
public class Main implements ReceptorProcessorModule, FlowProcessor, VeyronProcessingComponent {
    private DNS dns;
    private boolean doProc;
    private DNSFlow f;
    private WriteBuffer writeBuffer;
    
    public Main() {
        doProc = false;
        
        ComponentRegistry.getInstance().registerProcessingComponent(this);
    }
    
    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }
    
    /*
    private DNSFlowRecord dnsFlowToRecord(DNSFlow flow) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(32 + (int) flow.getBodyLength());
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            flow.writeToDataStream(dataStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        DNSFlowRecord record = new DNSFlowRecord(null);
        record.setRecordData(byteStream.toByteArray());
        
        return record;
    }
    */
    
    private DNSFlowRecord.DNSResponse dnsFlowResponseToResponse(DNSFlowResponse flowResponse) {
        DNSFlowRecord.DNSResponse response = new DNSFlowRecord.DNSResponse();
        
        response.setName(flowResponse.getName());
        response.setResponseType(flowResponse.getResponseType());
        response.setResourceData(flowResponse.getResourceData(), flowResponse.getResourceDataLength());
        response.setTTL(flowResponse.getTTL());
        
        return response;
    }
    
    private DNSFlowRecord dnsFlowToRecord(DNSFlow flow) {
        DNSFlowRecord record = new DNSFlowRecord(null);
        
        // Header
        record.setTemplateID(flow.getTemplateID());
        record.setBodyLength(flow.getBodyLength());
        record.setProtocol(flow.getProtocol());
        record.setSourcePort((char) flow.getSourcePort());
        record.setDestinationPort((char) flow.getDestinationPort());
        record.setSourceIP(flow.getSourceIP());
        record.setDestinationIP(flow.getDestinationIP());
        record.setStartTimeSeconds(flow.getStartTimeSeconds());
        record.setStartTimeMicroSeconds(flow.getStartTimeMicroSeconds());
        record.setEndTimeSeconds(flow.getEndTimeSeconds());
        record.setEndTimeMicroSeconds(flow.getEndTimeMicroSeconds());
        
        // Query section
        record.setQueryFlags(flow.getQueryFlags());
        record.setResponseFlags(flow.getResponseFlags());
        record.setName(flow.getName());
        record.setQueryType(flow.getQueryType());
        record.setAnswerCount(flow.getAnswerCount());
        record.setAuthorityCount(flow.getAuthorityCount());
        record.setAdditionalCount(flow.getAdditionalCount());
        
        // Answers
        DNSFlowResponse[] oldAnswers = flow.getAnswers();
        DNSFlowRecord.DNSResponse[] newAnswers = new DNSFlowRecord.DNSResponse[flow.getAnswerCount()];
        for (int i = 0; i < newAnswers.length; ++i) {
            newAnswers[i] = dnsFlowResponseToResponse(oldAnswers[i]);
        }
        record.setAnswers(newAnswers);
        
        // Authorities
        DNSFlowResponse[] oldAuthorities = flow.getAuthorities();
        DNSFlowRecord.DNSResponse[] newAuthorities = new DNSFlowRecord.DNSResponse[flow.getAuthorityCount()];
        for (int i = 0; i < newAuthorities.length; ++i) {
            newAuthorities[i] = dnsFlowResponseToResponse(oldAuthorities[i]);
        }
        record.setAuthorities(newAuthorities);
        
        // Additionals
        DNSFlowResponse[] oldAdditionals = flow.getAdditionals();
        DNSFlowRecord.DNSResponse[] newAdditionals = new DNSFlowRecord.DNSResponse[flow.getAdditionalCount()];
        for (int i = 0; i < newAdditionals.length; ++i) {
            newAdditionals[i] = dnsFlowResponseToResponse(oldAdditionals[i]);
        }
        record.setAdditionals(newAdditionals);
        
        return record;
    }
    
    public void newFlow(Flow flow) {
        if (!(doProc && flow instanceof DNSFlow)) {
            return;
        }
        
        f = (DNSFlow) flow;
        
        // convert DNSFlow to DNSFlowRecord
        DNSFlowRecord flowRecord = dnsFlowToRecord(f);
        //System.err.println("DNSCollect: slice before: " + (f.getEndTimeSeconds() >> DNS.TIME_SHIFT) + " and after: " + flowRecord.getTimeSlice());
        if (flowRecord == null) {
            System.err.println("DNSCollect: newFlow(): Error converting DNSFlow to DNSFlowRecord!");
            return;
        }
        
        // send record to buffer thread
        writeBuffer.addRecord(flowRecord);
    }

    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof DNSFlow;
    }

    public void initialize(NetworkTopology net, NetworkSymbols syms) {
        if (syms instanceof DNS) {
            dns = (DNS) syms;
            writeBuffer = new WriteBuffer(this.dns, 3);
            doProc = true;
            
            System.out.println("========== DNSCollect initialized ==========");
        }
    }

    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void start() {
        doProc = true;
    }

    public void stop() {
        doProc = false;
    }

    public void reset() {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public void setReporter(Reporter report) {
    }
    
    
    /*
    private long getIPFromPTRName(String name) {
        try {
            int index;
            if ((index = name.indexOf("in-addr.arpa")) == -1) {
                return 0;
            }
            
            long tmp = Util.getLongIp(name.substring(0, index - 1));
            long ip = 0;
            ip |= tmp & 0xFFL;
            for (int i = 1; i < 4; ++i) {
                tmp >>= 8;
                ip <<= 8;
                ip |= tmp & 0xFFL;
            }

            return ip;

        } catch (Exception e) {
            System.out.println(name);
            e.printStackTrace();
            return 0;
        }

    }
    */
}
