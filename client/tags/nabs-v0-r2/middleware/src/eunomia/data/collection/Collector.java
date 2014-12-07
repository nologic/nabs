/*
 * Collector.java
 *
 * Created on June 1, 2005, 12:49 PM
 */

package eunomia.data.collection;

import eunomia.receptor.module.interfaces.FlowModule;
import java.sql.*;
import org.apache.log4j.*;
import java.util.concurrent.Semaphore;
import eunomia.flow.*;


/**
 *
 * @author  Mikhail Sosonkin
 */
public class Collector implements Runnable, FlowProcessor {
    private Statement stmt;
    private StringBuilder b;
    private int bufferSize;
    private boolean firstFlow;
    private String table;
    private Thread thread;
    private Semaphore sem;
    private Object copyLock;
    private boolean doQuit;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Collector.class);
    }
    
    public Collector(Statement s, String t){
        doQuit = false;
        copyLock = new Object();
        sem = new Semaphore(1);
        stmt = s;
        table = t;
        bufferSize = 768*1024 - 2048;
        b = new StringBuilder(bufferSize + 1024);
        resetBuffer();
        thread = new Thread(this);
        thread.start();
        thread.setName("Inserter");
    }
    
    public void run() {
        while(!doQuit){
            try{
                Thread.sleep(200);
            } catch(Exception e){
            }
            
            dump();
        }
    }
    
    public void quit(){
        doQuit = true;
    }
    
    private void dump(){
        String query = null;
        synchronized(copyLock){
            if(!firstFlow){
                query = b.toString();
                resetBuffer();
            }
        }
        
        sem.release();
        
        try {
            if(query != null){
                stmt.executeUpdate(query);
            }
        } catch(SQLException e){
            logger.error("Unable to insert into database: " + e.getMessage());
        }
    }
    
    private void resetBuffer(){
        b.delete(0, b.length());
        firstFlow = true;
        b.append("INSERT INTO ");
        b.append(table);
        b.append(" VALUES ");
    }
    
    public void newFlow(Flow flow) {
        synchronized(copyLock){
            if(!firstFlow) {
                b.append(",");
            }
            firstFlow = false;
            b.append(flow.getSpecificInfo(null));
        }
        
        if(b.length() > bufferSize){
            thread.interrupt();
            sem.acquireUninterruptibly();
        }
    }
    
    public void setFilter(Filter filter) {
    }
    
    public Filter getFilter() {
        return null;
    }

    public boolean accept(FlowModule module) {
        return true;
    }
    
}
