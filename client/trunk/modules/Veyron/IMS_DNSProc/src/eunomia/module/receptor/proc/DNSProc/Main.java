/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.proc.DNSProc;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.flow.DNSFlow.DNSFlow;
import eunomia.module.receptor.flow.DNSFlow.DNSFlowResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author justin
 */
public class Main implements FlowProcessor, ReceptorProcessorModule {
    private boolean doProc;
    private Filter filter;
    
    public Main() {
        doProc = false;
        filter = new Filter();
    }
    
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void newFlow(Flow flow) {
        System.out.println("======== DNSProc newflow() called ========");
        /*
        System.out.print("doProc: ");
        System.out.println(doProc);
        System.out.print("instanceof DNSFlow: ");
        System.out.println(flow instanceof DNSFlow);
        System.out.print("filter.allow(): ");
        System.out.println(filter.allow(flow));
        */
        if (doProc && flow instanceof DNSFlow && filter.allow(flow)) {
            // print some testing info...
            DNSFlow f = (DNSFlow) flow;
            System.out.println(Util.getInetAddress(f.getSourceIP()).getHostAddress());
            System.out.println(f.getDestinationIP());
            System.out.println(f.getSourcePort());
            System.out.println(f.getDestinationPort());
            System.out.println(f.getName());
            System.out.println(f.getQueryTypeName());
            DNSFlowResponse[] responses = f.getResponses();
            for (int i = 0; i < f.getResponseCount(); ++i) {
                System.out.println("Response[" + i + "]:");
                System.out.println(responses[i].getName());
                if (responses[i].getResponseType() == DNSFlow.TYPE_A) {
                    System.out.println(responses[i].getResourceDataIP());
                } else if (responses[i].getResponseType() == DNSFlow.TYPE_MX) {
                    System.out.print(responses[i].getResourceDataMXPref());
                    System.out.println(responses[i].getResourceDataMXName());
                } else {
                    System.out.println(responses[i].getResourceDataName());
                    
                }
                System.out.println(responses[i].getTTL());
            }
        }
    }

    public boolean accept(FlowModule module) {
        return module.getNewFlowInstance() instanceof DNSFlow;
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
        if (name.equals("this")) {
            return this;
        }
        
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

}
