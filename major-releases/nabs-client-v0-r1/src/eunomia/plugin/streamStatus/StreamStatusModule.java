/*
 * StreamStatus.java
 *
 * Created on June 14, 2005, 7:55 PM
 */

package eunomia.plugin.streamStatus;
import eunomia.plugin.interfaces.*;

import org.jfree.data.general.*;

import java.util.*;
import javax.swing.*;
import eunomia.core.data.flow.*;
import eunomia.core.data.streamData.StreamDataSource;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamStatusModule implements Module, ModularFlowProcessor, Dataset, RefreshNotifier {
    private List list;
    private long events;
    private long streamSize;
    private long lastTime;
    private long lastEvents;
    private long lastStreamSize;
    private double streamRate;
    private double eventRate;
    private DatasetChangeEvent event;
    private DatasetGroup dg;
    private StreamStatusBar comp;

    public StreamStatusModule() {
        list = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        comp = new StreamStatusBar();
        comp.setStreamStatus(this);
    }
    
    public void updateData(){
        Iterator it = list.iterator();

        while(it.hasNext()){
            DatasetChangeListener l = (DatasetChangeListener)it.next();
            l.datasetChanged(event);
        }
    }
    
    public void removeChangeListener(DatasetChangeListener l) {
        list.remove(l);
    }
        
    public void addChangeListener(DatasetChangeListener l) {
        list.add(l);
    }
    
    public long getEvents(){
        return events;
    }
    
    public long getBytes(){
        return streamSize;
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
        streamSize += flow.getSize();
        ++events;
    }
    
    public void setFilter(Filter filter) {
    }

    public Filter getFilter() {
        return null;
    }
    
    public DatasetGroup getGroup() {
        return dg;
    }
    
    public void setGroup(DatasetGroup group) {
        dg = group;
    }
    
    public ModularFlowProcessor getFlowPocessor(){
        return this;
    }
    
    public JComponent getJComponent(){
        return comp;
    }
    
    public RefreshNotifier getRefreshNotifier(){
        return this;
    }

    public boolean allowFilters() {
        return false;
    }

    public boolean allowFullscreen() {
        return false;
    }

    public boolean allowToolbar() {
        return false;
    }

    public JComponent getControlComponent() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public boolean isConfigSeparate() {
        return false;
    }

    public boolean isControlSeparate() {
        return false;
    }

    public void showLegend(boolean b) {
    }

    public void showTitle(boolean b) {
    }

    public void setProperty(String name, Object value) {
        int i = 0;
        
        try {
            i = Integer.parseInt(value.toString());
            comp.setOrientation(i);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Object getProperty(String name) {
        return "" + comp.getOrientation();
    }

    public void start() {
    }

    public void stop() {
    }

    public void reset() {
    }

    public void setStream(StreamDataSource sds) {
    }
}