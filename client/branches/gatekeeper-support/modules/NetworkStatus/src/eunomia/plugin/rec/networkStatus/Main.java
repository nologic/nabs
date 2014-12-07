/*
 * MainLoaded.java
 *
 * Created on May 20, 2007, 6:51 PM
 *
 */

package eunomia.plugin.rec.networkStatus;

import eunomia.flow.Filter;
import com.vivic.eunomia.module.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorModule;
import com.vivic.eunomia.module.receptor.FlowModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorModule, FlowProcessor {
    private static final byte LOSSY1 = 0x0;
    private static final byte LOSSY2 = 0x1;
    private static final byte PIECHT = 0x2;
    private static final byte HOSTDT = 0x3;
    
    private eunomia.plugin.rec.pieChart.Main pieChart;
    private eunomia.plugin.rec.lossyHistogram.Main lcUploaders;
    private eunomia.plugin.rec.lossyHistogram.Main lcDownloaders;
    private eunomia.plugin.rec.hostDetails.Main hostDetails;
    
    private FlowProcessor pieProc;
    private FlowProcessor lcupProc;
    private FlowProcessor lcdoProc;
    private FlowProcessor hostProc;

    public Main() {
        pieChart = new eunomia.plugin.rec.pieChart.Main();
        lcUploaders = new eunomia.plugin.rec.lossyHistogram.Main();
        lcDownloaders = new eunomia.plugin.rec.lossyHistogram.Main();
        hostDetails = new eunomia.plugin.rec.hostDetails.Main();

        pieProc = pieChart.getFlowProcessor();
        lcupProc = lcUploaders.getFlowProcessor();
        lcdoProc = lcDownloaders.getFlowProcessor();
        hostProc = hostDetails.getFlowProcessor();
        
        lcUploaders.setProperty(eunomia.plugin.rec.lossyHistogram.Main.CMD_DEST_HOST_MV, hostProc);
        lcDownloaders.setProperty(eunomia.plugin.rec.lossyHistogram.Main.CMD_DEST_HOST_MV, hostProc);
        lcDownloaders.setComparator(false, true, false, false, false);
    }

    public void destroy() {
        pieChart.destroy();
        lcUploaders.destroy();
        lcDownloaders.destroy();
        hostDetails.destroy();
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
        lcUploaders.updateStatus(out);
        lcDownloaders.updateStatus(out);
        pieChart.updateStatus(out);
        hostDetails.updateStatus(out);
    }

    public void setControlData(InputStream in) throws IOException {
        lcUploaders.setControlData(in);
        lcDownloaders.setControlData(in);
        pieChart.setControlData(in);
        hostDetails.setControlData(in);
    }

    public void getControlData(OutputStream out) throws IOException {
        lcUploaders.getControlData(out);
        lcDownloaders.getControlData(out);
        pieChart.getControlData(out);
        hostDetails.getControlData(out);
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
        byte b = (byte)in.read();
        out.write(b);
        
        switch(b) {
            case LOSSY1:
                lcUploaders.processMessage(in, out);
                break;
                
            case LOSSY2:
                lcDownloaders.processMessage(in, out);
                break;
                
            case PIECHT:
                break;
                
            case HOSTDT:
                hostDetails.processMessage(in, out);
                break;
        }
    }

    public void start() {
        pieChart.start();
        lcUploaders.start();
        lcDownloaders.start();
        hostDetails.start();
    }

    public void stop() {
        pieChart.stop();
        lcUploaders.stop();
        lcDownloaders.stop();
        hostDetails.stop();
    }

    public void reset() {
        pieChart.reset();
        lcUploaders.reset();
        lcDownloaders.reset();
        hostDetails.reset();
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

    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }

    public void newFlow(Flow flow) {
        pieProc.newFlow(flow);
        lcupProc.newFlow(flow);
        lcdoProc.newFlow(flow);
        hostProc.newFlow(flow);
    }

    public boolean accept(FlowModule module) {
        return pieProc.accept(module) && lcupProc.accept(module) &&
               lcdoProc.accept(module) && hostProc.accept(module);
    }
}