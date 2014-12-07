/*
 * StreamStatus.java
 *
 * Created on June 14, 2005, 7:55 PM
 */

package eunomia.plugin.rec.streamStatus;

import eunomia.flow.*;
import eunomia.messages.Message;
import eunomia.messages.module.ModuleMessage;
import eunomia.plugin.interfaces.*;
import eunomia.flow.Filter;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.receptor.module.interfaces.FlowModule;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class Main implements ReceptorModule, FlowProcessor {
    private long events;
    private long streamSize;
    private long lastTime;
    private long lastEvents;
    private long lastStreamSize;
    private double streamRate;
    private double eventRate;
    private Method method;

    public Main() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run(){
                computeRates();
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
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
        streamSize += ((NABFlow)flow).getSize();
        ++events;
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
}