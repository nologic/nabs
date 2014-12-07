/*
 * FilterList.java
 *
 * Created on August 1, 2005, 3:42 PM
 *
 */

package eunomia.flow;

import eunomia.messages.FilterEntryMessage;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */

public class FilterList {
    private FilterEntry[] entries;
    private FilterEntry[] listEntries;
    private int count;
    
    public FilterList() {
        entries = new FilterEntry[10];
        count = 0;
    }
    
    public void clearList() {
        count = 0;
        Arrays.fill(entries, null);
    }
    
    public FilterEntry[] getArray(){
        return entries;
    }
    
    public int getCount(){
        return count;
    }
    
    public List getAsList(){
        return Arrays.asList(getAsArray());
    }
    
    public FilterEntryMessage[] getAsMessageArray() {
        FilterEntryMessage[] msgs = null;
        FilterEntry[] arr = getAsArray();
        
        if(arr != null) {
            msgs = new FilterEntryMessage[arr.length];
            for(int i = 0; i < arr.length; i++){
                msgs[i] = arr[i].getFilterEntryMessage();
            }
        }
        
        return msgs;
    }
    
    public FilterEntry[] getAsArray(){
        if(listEntries == null || listEntries.length != count){
            listEntries = new FilterEntry[count];
        }
        
        for(int i = entries.length - 1, k = 0; i != -1 && k < count; --i){
            if(entries[i] != null){
                listEntries[k++] = entries[i];
            }
        }
        
        return listEntries;
    }
    
    public void addEntry(FilterEntry f){
        int c = count;
        if(c == entries.length){
            FilterEntry[] newEntries = new FilterEntry[c * 2];
            System.arraycopy(entries, 0, newEntries, c, c);
            entries = newEntries;
        }
        
        for(int i = entries.length - 1; i != -1; --i){
            if(entries[i] == null){
                entries[i] = f;
                ++count;
                break;
            }
        }
    }
    
    public void removeEntry(FilterEntry f){
        for(int i = entries.length - 1; i != -1; --i){
            if(entries[i] == f){
                entries[i] = null;
                --count;
                break;
            }
        }
    }
    
    public void switchEntries(FilterEntry f1, FilterEntry f2){
        int fi1 = -1;
        int fi2 = -1;

        //find both
        for(int i = entries.length - 1; i != -1; --i){
            FilterEntry t = entries[i];
            if(t == f1){
                fi1 = i;
            } else if(t == f2){
                fi2 = i;
            } else if(fi1 != -1 && fi2 != -1){
                break;
            }
        }
        
        //perform the switch
        if(fi1 != -1 && fi2 != -1){
            FilterEntry tmp = entries[fi1];
            entries[fi1] = entries[fi2];
            entries[fi2] = tmp;
        }
    }
}