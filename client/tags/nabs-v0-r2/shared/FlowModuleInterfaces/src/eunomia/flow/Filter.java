/*
 * Filter.java
 *
 * Created on July 22, 2005, 3:16 PM
 *
 */

package eunomia.flow;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Filter {
    private FilterList blackList;
    private FilterList whiteList;
    private Object synchObject;
    
    public Filter() {
        synchObject = new Object();
        blackList = new FilterList();
        whiteList = new FilterList();
    }
    
    public void addFilterBlack(FilterEntry f){
        blackList.addEntry(f);
    }
    
    public FilterList getBlackList(){
        return blackList;
    }
    
    public void removeFilterBlack(FilterEntry f){
        blackList.removeEntry(f);
    }
    
    public void addFilterWhite(FilterEntry f){
        whiteList.addEntry(f);
    }
    
    public FilterList getWhiteList(){
        return whiteList;
    }
    
    public void removeFilterWhite(FilterEntry f){
        whiteList.removeEntry(f);
    }

    public boolean allow(Flow flow){
        int bListEntries = blackList.getCount();
        int wListEntries = whiteList.getCount();

        if(bListEntries == 0 && wListEntries == 0){
            return true;
        }

        FilterEntry[] bList = blackList.getArray();
        FilterEntry[] wList = whiteList.getArray();

        long sIp = flow.getSourceIP();
        long dIp = flow.getDestinationIP();
        int sPort = flow.getSourcePort();
        int dPort = flow.getDestinationPort();
        int i;
        int listLen = bList.length;
        //int type = flow.getType();
        
        for(i = listLen - 1; i != -1; --i){
            FilterEntry entry = bList[i];
            if(entry != null){
                if(entry.inRange(sIp, dIp, sPort, dPort) && entry.inRangeFlow(flow)){
                    return false;
                }
            }
        }

        listLen = wList.length;
        for(i = listLen - 1; i != -1; --i){
            FilterEntry entry = wList[i];
            if(entry != null){
                if(entry.inRange(sIp, dIp, sPort, dPort) && entry.inRangeFlow(flow)){
                    return true;
                }
            }
        }
        if(wListEntries != 0){
            return false;
        }

        return true;
    }
}