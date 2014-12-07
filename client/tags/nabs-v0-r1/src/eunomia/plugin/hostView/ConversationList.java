/*
 * ConversationList.java
 *
 * Created on August 23, 2005, 2:37 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.core.data.flow.*;
import eunomia.util.number.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ConversationList {
    private HashMap hostsMap;
    private ModLong modLong;
    private ModInteger modInt;
    private LinkedList allConversations;
    private boolean hasChanged;
    private Conversation[] convArr;
    private Object lockObject;
    
    private long gcTime;
    private long convTime;
    private long lastTime;
    
    public ConversationList() {
        hostsMap = new HashMap();
        modLong = new ModLong();
        modInt = new ModInteger();
        allConversations = new LinkedList();
        hasChanged = false;
        lockObject = new Object();
        lastTime = System.currentTimeMillis();
    }
    
    public void setGCTimeInterval(long timeToCollect, long timeForConv){
        gcTime = timeToCollect;
        convTime = timeForConv;
    }
    
    private void gc(){
        long curTime = System.currentTimeMillis();
        
        if( (curTime - lastTime) > gcTime){
            int collected = 0;
            lastTime = curTime;
            
            Conversation[] cons = getArray();
            for(int i = 0; i < cons.length; i++){
                Conversation c = cons[i];

                long l = c.getLastActive();
                if( (curTime - l) > convTime){
                    collected++;
                    removeConversation(c.getRmt_ip(), c.getLcl_port(), c.getRmt_port());
                }
            }
        }
    }
    
    public int convCount(){
        return allConversations.size();
    }
    
    public Iterator getIterator(){
        return Arrays.asList(getArray()).iterator();
    }
    
    /*
     * Note: When the list of connections grows large copying will be longer
     * and longer, so it's better to return the previous set then to lock and
     * wait until the copy is completed, later this will be changed to using an
     * array so that the problem is not here and done w/o synch.
     *
     * large is 3000+ connecations.
     */
    public Conversation[] getArray(){
        if(hasChanged || convArr == null){
            hasChanged = false;
            try {
                Conversation[] tmp = (Conversation[])allConversations.toArray(new Conversation[]{});
                convArr = tmp;
            } catch(Exception e){
            }
        }
        return convArr;
    }
    
    public Conversation insertNewConversation(long rmt_ip, int lcl_port, int rmt_port){
        gc();
        
        Conversation conv = new Conversation(rmt_ip, lcl_port, rmt_port);
        
        modLong.setLong(rmt_ip);
        modInt.setInt((lcl_port << 16) | (rmt_port & 0xFFFF));
        
        ModInteger mInt = new ModInteger();
        mInt.setInt((lcl_port << 16) | (rmt_port & 0xFFFF));

        HashMap portsMap = (HashMap)hostsMap.get(modLong);
        if(portsMap == null){
            ModLong mLong = new ModLong();

            mLong.setLong(rmt_ip);
            portsMap = new HashMap();
            hostsMap.put(mLong, portsMap);
        }
        portsMap.put(mInt, conv);
        allConversations.add(conv);
        hasChanged = true;

        return conv;
    }
    
    public Conversation removeConversation(long rmt_ip, int lcl_port, int rmt_port){
        modLong.setLong(rmt_ip);
        modInt.setInt((lcl_port << 16) | (rmt_port & 0xFFFF));
        
        HashMap portsMap = (HashMap)hostsMap.get(modLong);
        Conversation conv = null;
        
        if(portsMap != null){
            conv = (Conversation)portsMap.remove(modInt);
            if(conv != null){
                if(portsMap.size() == 0){
                    hostsMap.remove(modLong);
                }
            }
        }
        
        allConversations.remove(conv);
        
        return conv;
    }
    
    public Conversation findConversation(long rmt_ip, int lcl_port, int rmt_port){
        modLong.setLong(rmt_ip);
        modInt.setInt((lcl_port << 16) | (rmt_port & 0xFFFF));
        
        Conversation conv = null;
        
        HashMap portsMap = (HashMap)hostsMap.get(modLong);
        if(portsMap != null){
            conv = (Conversation)portsMap.get(modInt);
        }
        
        if(conv != null){
            return conv;
        }
        
        return insertNewConversation(rmt_ip, lcl_port, rmt_port);
    }
}