package eunomia.module.receptor.proc.spammer;

import eunomia.config.Config;
import eunomia.module.common.proc.spammer.MailServer;
import eunomia.receptor.module.NABFlowV2.NABFlowV2;
import eunomia.util.number.ModInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mikhail Sosonkin.
 */

// It seems that this module should just track Mail servers and 
// the median and spammer identification should be done in another
// module, possibly the Analysis module. For now all will be done
// here.

public class MailTracker {
    private static final long SECOND = 1;
    public static final long MINUTE = 60 * SECOND;
    public static final long TEN_MINUTES = 10 * MINUTE;
    public static final long THIRTY_MINUTES = 30 * MINUTE;
    public static final long HOUR = 60 * MINUTE;
    public static final long[] INTERVALS = {MINUTE, TEN_MINUTES, THIRTY_MINUTES, HOUR};
    
    private Map mailServers;
    private int median;
    private long lastFlowTime;
    private long lastComputed;
    private ModInteger servRetriever;
    private boolean isComputing;
    private int curInterval;
    
    private List listeners;

    public MailTracker() {
        isComputing = false;
        curInterval = 0;
        lastFlowTime = -1;
        servRetriever = new ModInteger();
        mailServers = new HashMap();
        listeners = new ArrayList();

        Config conf = Config.getConfiguration("eunomia.module.spammer");

        median = conf.getInt("median", median);
        lastComputed = Long.parseLong(conf.getString("last", "0"));
    }
    
    public void addMailTrackerLintener(MailTrackerListener l) {
        listeners.add(l);
    }
    
    public void removeMailTrackerLintener(MailTrackerListener l) {
        listeners.remove(l);
    }
    
    private void fireSpammerAppeared(MailServer serv) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            MailTrackerListener l = (MailTrackerListener) it.next();
            l.spammerAppeared(serv);
        }
    }
    
    private void fireSpammerDisappeared(MailServer serv) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            MailTrackerListener l = (MailTrackerListener) it.next();
            l.spammerDisappeared(serv);
        }
    }
    
    private void fireMailServerAppeared(MailServer serv) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            MailTrackerListener l = (MailTrackerListener) it.next();
            l.mailServerAppeared(serv);
        }
    }
    
    private void fireMailServerDisappeared(MailServer serv) {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            MailTrackerListener l = (MailTrackerListener) it.next();
            l.mailServerDisappeared(serv);
        }
    }
    
    private void checkMedian() {
        if(!isComputing && lastFlowTime - lastComputed > INTERVALS[curInterval]) {
            isComputing = true;
            lastComputed = lastFlowTime;
            
            // wait longer next time.
            if(curInterval < INTERVALS.length - 1) {
                curInterval++;
            }
            
            new Thread(new MedianComputer(lastFlowTime, getSnapShotList())).start();
        }
    }
    
    public int getMedian() {
        return median;
    }
    
    public long getLastFlowTime() {
        return lastFlowTime;
    }
    
    public MailServer[] getSnapShotList() {
        // TODO: Need to syncronize this somehow.
        return (MailServer[])mailServers.values().toArray(new MailServer[]{});
    }
    
    public MailServer getMailServer(long ip) {
        ModInteger integer = new ModInteger();
        integer.setInt((int)ip);
        
        return (MailServer)mailServers.get(integer);
    }

    private void recomputeMedian(long lastFlowTimeSnapShot, MailServer[] list) {
        int[] counts = new int[list.length];
        int newMedian = 0;
        double avg = 0.0;

        for(int i = 0; i < list.length; ++i) {
            // get counts for the last hour.
            int count = list[i].getTotalConnectionCount(lastFlowTimeSnapShot, HOUR);
            
            counts[i] = count;
            avg += count;
        }
        avg /= counts.length;
        
        //compute the actual median
        double lastDiff = Integer.MAX_VALUE;
        for(int i = 0; i < counts.length; ++i) {
            double diff = Math.abs(avg - (double)counts[i]);
            
            if(diff < lastDiff) {
                lastDiff = diff;
                newMedian = counts[i];
            }
        }
        
        setMedian(newMedian);
        
        isComputing = false;
    }

    private void setMedian(int med) {
        median = med;
        lastComputed = System.currentTimeMillis();

        Config conf = Config.getConfiguration("eunomia.module.spammer");
        conf.setInt("median", median);
        conf.setString("last", Long.toString(lastComputed));
        conf.save();
        
        System.out.println("New median: " + median);
    }

    private MailServer getServer(long ip) {
        servRetriever.setInt((int)ip);

        MailServer serv = (MailServer)mailServers.get(servRetriever);
        if(serv == null) {
            serv = new MailServer(ip);
            
            mailServers.put(serv, serv);
            this.fireMailServerAppeared(serv);
        }

        return serv;
    }

    public void examineFlow(NABFlowV2 flow, boolean hasEnded) {
        MailServer serv = getServer(flow.getSourceIP());

        long endTime = (hasEnded ? flow.getEndTimeSeconds() : -1);
        serv.updateInstance(flow.getDestinationIP(), flow.getSourcePort(), flow.getStartTimeSeconds(), endTime);

        endTime = flow.getEndTimeSeconds();
        if(endTime > lastFlowTime) {
            lastFlowTime = endTime;
            
            checkMedian();
        }
    }
    
    private class MedianComputer implements Runnable {
        private long lastFlowTimeSnapShot;
        private MailServer[] mailServers;
        
        public MedianComputer(long lastFlowTime, MailServer[] listSnapShot) {
            lastFlowTimeSnapShot = lastFlowTime;
            mailServers = listSnapShot;
        }
        
        public void run() {
            System.out.println("Starting thread");
            recomputeMedian(lastFlowTimeSnapShot, mailServers);
        }
    }
}