/*
 * ShellHistory.java
 *
 * Created on September 19, 2006, 9:40 PM
 *
 */

package eunomia.shell;

import eunomia.config.Config;
import java.io.IOException;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ShellHistory {
    private static int commandHistory = 256;
    
    private String[] list;
    private int current;
    private int last_index;
    
    public ShellHistory() {
        list = new String[commandHistory];
        last_index = 0;
    }
    
    public void addItem(String item){
        current = 0;
        
        if(item.equals(list[current + 1])) {
            return;
        }
        
        for(int i = last_index; i != 0; --i){
            list[i + 1] = list[i];
        }
        
        if(last_index < commandHistory - 1){
            ++last_index;
        }
        
        list[1] = item;
    }
    
    public String getOlder(){
        if(current < last_index) {
            return list[++current];
        }
        
        return list[current];
    }
    
    public String getNewer(){
        if(current > 0){
            return list[--current];
        }
        
        return list[0];
    }
    
    public void save(String shell) throws IOException {
        Config config = Config.getConfiguration("shell." + shell);
        
        String[] tmp = new String[last_index];
        System.arraycopy(list, 1, tmp, 0, tmp.length);
        config.setArray("history", tmp);
        config.save();
    }
    
    public void load(String shell){
        Config config = Config.getConfiguration("shell." + shell);
        
        Object[] h = config.getArray("history", null);
        if(h != null){
            int len = h.length;
            if(len > commandHistory - 1){
                len = commandHistory - 1;
            }
            
            System.arraycopy(h, 0, list, 1, len);
            current = 0;
            if(len > 0){
                last_index = len;
            }
        }
    }
}