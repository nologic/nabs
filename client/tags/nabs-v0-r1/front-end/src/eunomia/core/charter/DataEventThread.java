/*
 * DataEventThread.java
 *
 * Created on June 14, 2005, 5:53 PM
 */

package eunomia.core.charter;

import java.util.*;
import org.apache.log4j.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class DataEventThread implements Runnable {
    private List list;
    private int sleepInt;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(DataEventThread.class);
    }
    
    public DataEventThread(int st) {
        list = new LinkedList();
        sleepInt = st;
        new Thread(this);
    }
    
    public void addCharter(DataEventNotifier c){
        list.add(c);
    }
    
    public void removeCharter(DataEventNotifier c){
        list.remove(c);
    }
    
    public DataEventThread() {
        this(1000);
    }
    
    public void setSleepTime(int st){
        sleepInt = st;
    }
    
    public int getSleepTime(){
        return sleepInt;
    }
    
    public void run() {
        while(true){
            try {
                Thread.sleep(sleepInt);
            } catch(Exception e){
            }
            
            Object[] datas = list.toArray();
            for(int i = 0; i < datas.length; i++){
                try {
                    ((DataEventNotifier)datas[i]).updateData();
                } catch(Exception e){
                    logger.warn("Error updataing: " + datas[i]);
                    e.printStackTrace();
                }
            }
        }
    }
}