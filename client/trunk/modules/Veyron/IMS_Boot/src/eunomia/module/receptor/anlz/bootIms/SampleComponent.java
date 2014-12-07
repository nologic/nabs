/*
 * SampleComponent.java
 *
 * Created on February 3, 2008, 8:09 PM
 *
 */

package eunomia.module.receptor.anlz.bootIms;

import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import eunomia.module.receptor.libb.imsCore.iterators.ChannelEntityFilter;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.SelectSpec;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SampleComponent implements VeyronAnalysisComponent {
    private NetworkTopology net;
    
    private int hosts;
    private int chans;
    private int traveralTime;
    private double traveralRate;
    private double avgCon;
    
    public SampleComponent() {
    }
    
    public void initialize(NetworkTopology net, NetworkSymbols syms) {
        this.net = net;
    }
    
    public void executeAnalysis() {
        hosts = net.getHostCount();
        chans = net.getChannelCount();
        
        // Compute average connection count in the last 60 minutes of activity
        long activ = net.getLastActivity();
        long startTime = activ - 60L*60L;
        
        int conSum = 0;
        int hostCount = 0;
        
        try {
            long time = System.currentTimeMillis();
            
            //SelectSpec timeFrame = new SelectSpec(startTime, activ, SelectSpec.START_ACTIVITY);
            SelectSpec timeFrame = new SelectSpec(startTime, activ, SelectSpec.LAST_ACTIVITY);
            System.out.println("Getting ent");
            Iterator hosts = net.getEntities(timeFrame);
            while (hosts.hasNext()) {
                NetworkEntity ent = (NetworkEntity) hosts.next();
                
                hostCount++;

                Iterator it = net.getChannelsForEntity(timeFrame, ent.getHostKey(), ChannelEntityFilter.EITHER_ENTITY);
                while (it.hasNext()) {
                    NetworkChannel chan = (NetworkChannel) it.next();
                    conSum++;
                }
            }

            long took = System.currentTimeMillis() - time;
            
            avgCon = (double)conSum/(double)hostCount;
            traveralTime = (int)(took / 1000L);
            if(traveralTime != 0) {
                traveralRate = (conSum + hostCount) / traveralTime;
            }
            System.out.println("Avg: " + avgCon + " " + hostCount + " " + conSum + " rate " + traveralRate);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setReporter(Reporter report) {
    }

    public int getHosts() {
        return hosts;
    }

    public int getChans() {
        return chans;
    }

    public int getTraveralTime() {
        return traveralTime;
    }

    public double getTraveralRate() {
        return traveralRate;
    }

    public double getAvgCon() {
        return avgCon;
    }
}