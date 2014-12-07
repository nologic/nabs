/*
 * StreamStatus.java
 *
 * Created on June 14, 2005, 7:55 PM
 */

package eunomia.plugin.rec.streamStatus;

import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.FlowModule;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import com.vivic.eunomia.module.receptor.ReceptorProcessorModule;
import eunomia.util.io.EunomiaObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class Main implements ReceptorProcessorModule, FlowProcessor {
    private long events;
    private long streamSize;
    private long lastTime;
    private long lastEvents;
    private long lastStreamSize;
    private double streamRate;
    private double eventRate;
    private Method method;

    public Main() {
    }
    
    public long getEvents(){
        return events;
    }
    
    public long getBytes(){
        return streamSize;
    }
    
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name) {
        return null;
    }
    
    public void computeRates(){
        long tmpEvents = events;
        long stream = streamSize;
        
        double eventDiff = (double)(tmpEvents - lastEvents);
        double streamDiff = (double)(stream - lastStreamSize);
        
        long time = System.currentTimeMillis();
        double timeDiff = ((double)(time - lastTime))/1000.0;
        lastTime = time;
        
        lastEvents = tmpEvents;
        lastStreamSize = stream;
        
        streamRate = (streamDiff)/(timeDiff);
        eventRate = eventDiff/(timeDiff);
    }
    
    public double getEventRate(){
        return eventRate;
    }
    
    public double getByteRate(){
        return streamRate;
    }

    public void newFlow(Flow flow) {
        ++events;
        streamSize += flow.getSize();
    }
    
    public Filter getFilter() {
        return null;
    }
    
    public void setFilter(Filter f){
    }

    public FlowProcessor getFlowPocessor(){
        return this;
    }
    
    public void start() {
    }

    public void stop() {
    }

    public void reset() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }

    public void updateStatus(OutputStream out) throws IOException {
        computeRates();
        
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeLong(events);
        dout.writeLong(streamSize);
        dout.writeDouble(streamRate);
        dout.writeDouble(eventRate);
    }
    
    public void setControlData(InputStream in) throws IOException {
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }

    public Message processMessage(ModuleMessage msg) throws IOException {
        return null;
    }

    public void initialize() {
    }

    public void destroy() {
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public boolean accept(FlowModule module) {
        return true;
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }
}