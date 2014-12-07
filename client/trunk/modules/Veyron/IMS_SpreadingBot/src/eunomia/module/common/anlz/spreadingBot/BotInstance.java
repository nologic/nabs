/*
 * BotInstance.java
 *
 * Created on November 20, 2007, 9:49 PM
 *
 */

package eunomia.module.common.anlz.spreadingBot;

import eunomia.module.common.proc.ccChannels.ChannelFlow;
import eunomia.module.common.proc.spammer.MailServer;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class BotInstance {
    private long ip;
    private LinkedList dsAccesses;
    private List channels;
    private MailServer mailer;
    
    public BotInstance(long ip) {
        dsAccesses = new LinkedList();
        this.ip = ip;
    }
    
    public void addControlChannel(ChannelFlow cf) {
        channels.add(cf);
    }
    
    public void removeControlChannel(ChannelFlow cf) {
        channels.remove(cf);
    }
    
    public List getControlChannels() {
        return channels;
    }
    
    public void addDarkspaceAccess(long dest, long time) {
        dsAccesses.addFirst(new DarkspaceAccess(dest, time));
        
        // this stuff should be databased otherwise we'll run out of memory
        //  if the bot is scanning. For now keep only the last 4096
        if(dsAccesses.size() > 4096) {
            for (int i = 0; i < 500; i++) {
                dsAccesses.removeLast();
            }
        }
    }
    
    public List getDarkspaceAccesses() {
        return dsAccesses;
    }
    
    public long getIP() {
        return ip;
    }
    
    public int hashCode() {
        return (int)ip;
    }
    
    public boolean equals(Object o) {
        return ((BotInstance)o).ip == ip;
    }
 
    public MailServer getMailer() {
        return mailer;
    }

    public void setMailer(MailServer mailer) {
        this.mailer = mailer;
    }

    public class DarkspaceAccess {
        private long destination;
        private long time;

        DarkspaceAccess(long ip, long time) {
            destination = ip;
            this.time = time;
        }
        
        public long getDestination() {
            return destination;
        }

        public long getTime() {
            return time;
        }
    }
}