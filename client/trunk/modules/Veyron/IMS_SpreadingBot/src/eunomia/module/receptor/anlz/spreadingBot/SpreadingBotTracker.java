/*
 * SpreadingBotTracker.java
 *
 * Created on November 20, 2007, 9:14 PM
 *
 */

package eunomia.module.receptor.anlz.spreadingBot;

import eunomia.module.common.anlz.spreadingBot.BotInstance;
import eunomia.module.common.proc.ccChannels.ChannelFlow;
import eunomia.module.common.proc.spammer.MailServer;
import eunomia.module.receptor.proc.ccChannels.ControlChannelListener;
import eunomia.module.receptor.proc.darkspace.DarkspaceListener;
import eunomia.module.receptor.proc.spammer.MailTrackerListener;
import com.vivic.eunomia.sys.util.Util;
import eunomia.util.number.ModInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SpreadingBotTracker implements ControlChannelListener, DarkspaceListener, MailTrackerListener {
    private ModInteger retriever;
    private Map botMap;
    
    public SpreadingBotTracker() {
        retriever = new ModInteger();
        botMap = new HashMap();
    }
    
    private BotInstance getBot(long ip, boolean create) {
        BotInstance bot;
        
        retriever.setInt((int)ip);
        
        bot = (BotInstance)botMap.get(retriever);
        if(bot == null && create) {
            bot = new BotInstance(ip);
            botMap.put(new Integer((int)ip), bot);
        }
        
        return bot;
    }

    public void channelInitiated(ChannelFlow cf) {
        System.out.println("CC: " + cf);
        BotInstance bot1 = getBot(cf.getIp1(), true);
        BotInstance bot2 = getBot(cf.getIp2(), true);
        
        bot1.addControlChannel(cf);
        bot2.addControlChannel(cf);
    }

    public void channelTerminated(ChannelFlow cf) {
    }

    public void channelActive(ChannelFlow cf) {
    }

    public void darkspaceAccess(long src_live_ip, long dst_dark_ip, long time) {
        System.out.println("DS: " + Util.ipToString(src_live_ip) + " --> " + Util.ipToString(dst_dark_ip));
        BotInstance bot = getBot(src_live_ip, false);
        bot.addDarkspaceAccess(dst_dark_ip, time);
    }

    public void spammerAppeared(MailServer serv) {
        BotInstance bot = getBot(serv.getIp(), true);
        
        bot.setMailer(serv);
    }

    public void spammerDisappeared(MailServer serv) {
        BotInstance bot = getBot(serv.getIp(), true);
        
        bot.setMailer(null);
    }

    public void mailServerAppeared(MailServer serv) {
    }

    public void mailServerDisappeared(MailServer serv) {
    }
    
    public List getBots() {
        List list = new LinkedList();
        
        // this needs to be syched with getBot
        Iterator it = botMap.values().iterator();
        while (it.hasNext()) {
            BotInstance b = (BotInstance) it.next();
            
            if( (b.getControlChannels().size() > 0 && b.getMailer() != null) || 
                (b.getControlChannels().size() > 0 && b.getDarkspaceAccesses().size() > 0) ) {
                list.add(b);
            }
        }
        
        return list;
    }
}