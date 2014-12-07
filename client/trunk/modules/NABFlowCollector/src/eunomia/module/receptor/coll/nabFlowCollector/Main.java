/*
 * Main.java
 *
 * Created on December 27, 2006, 8:14 PM
 *
 */

package eunomia.module.receptor.coll.nabFlowCollector;

import eunomia.data.Database;
import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.module.flow.Flow;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import eunomia.plugin.interfaces.CollectionModule;
import eunomia.receptor.module.NABFlow.NABFlow;
import com.vivic.eunomia.module.flow.FlowModule;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements CollectionModule, FlowProcessor, Runnable {
    private Statement stmt;
    private StringBuilder b;
    private int bufferSize;
    private boolean firstFlow;
    private String table;
    private Thread thread;
    private Semaphore sem;
    private Object copyLock;
    private boolean doQuit;
    private Database db;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }

    public Main() {
    }

    public void setDatabase(Database db) throws SQLException {
        this.db = db;
        doQuit = false;
        copyLock = new Object();
        sem = new Semaphore(1);
        stmt = db.getNewStatement();
        table = db.getMainTable();
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
                Thread.sleep(1000);
            } catch(Exception e){
            }
            
            if(db.isConnected()) {
                dump();
            }
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
        Flow flow = module.getNewFlowInstance();
        return flow instanceof NABFlow;
    }    

    public void destroy() {
    }

    public FlowProcessor getFlowProcessor() {
        return this;
    }
}