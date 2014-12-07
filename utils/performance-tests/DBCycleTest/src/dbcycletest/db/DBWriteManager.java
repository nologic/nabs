package dbcycletest.db;

import com.sleepycat.db.DatabaseType;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author Justin Stallard
 */
public class DBWriteManager extends Thread {
    
    private boolean useEnvironment;
    private boolean useSecondaries;
    private boolean running;
    private String path;
    private int poolSize;
    private int nextIndex;
    
    private Lock runningLock;
    
    private DatabaseType primaryType;
    
    // queue of ready dbs
    private LinkedList<TestDatabase> ready;
    private Lock readyLock;
    private Condition readyCondition;
    
    // queue of dbs to be closed
    private LinkedList<TestDatabase> waiting;
    private Lock waitingLock;
    private Condition waitingCondition;
    
    private DBReadManager readManager;
    
    public DBWriteManager(String path, int poolSize, DBReadManager readManager,
                          boolean useEnvironment, boolean useSecondaries) {
        
        this.useEnvironment = useEnvironment;
        this.useSecondaries = useSecondaries;
        this.path = path;
        this.poolSize = poolSize;
        this.readManager = readManager;
        
        // default databaseType is queue
        primaryType = DatabaseType.QUEUE;
        
        // initialize array of DBs
        ready = new LinkedList<TestDatabase>();
        waiting = new LinkedList<TestDatabase>();

        readyLock = new ReentrantLock();
        readyCondition = readyLock.newCondition();
        waitingLock = new ReentrantLock();
        waitingCondition = waitingLock.newCondition();
        
        runningLock = new ReentrantLock();
                
        //System.err.println("DBWriteManager initialized. path = " + this.path + " nextIndex = " + nextIndex);
    }
    
    public boolean setPrimaryType(DatabaseType primaryType) {
        if (running) {
            return false;
        }
        
        this.primaryType = primaryType;
        return true;
    }
    
    // run() will wait for condition
    @Override
    public void run() {
        readyLock.lock();
        //System.err.println("DBWriteManager: run() got ready lock...");
        TestDatabase tmpDB;
        for (nextIndex = 0; nextIndex < poolSize; ++nextIndex) {
            tmpDB = new TestDatabase(path + "/writeEnv", nextIndex);
            tmpDB.setUseEnvironment(useEnvironment);
            tmpDB.setUseSecondaries(useSecondaries);
            tmpDB.setPrimaryType(primaryType);
            if (!tmpDB.open(false)) {
                System.err.println("Error opening database! Exiting...");
                System.exit(1);
            }
            
            ready.addLast(tmpDB);
        }
        readyCondition.signal();
        readyLock.unlock();
        
        running = true;
        runningLock.lock();
        while (running) {
            runningLock.unlock();
            waitingLock.lock();
            try {
                while (waiting.isEmpty()) {
                    waitingCondition.await();
                }
                runningLock.lock();
                if (!running) {
                    break;
                }
                runningLock.unlock();
                tmpDB = waiting.removeFirst();
                waitingLock.unlock();
                //System.err.print("DBManager: closing database...");
                tmpDB.close();
                //System.err.println("complete.");
                
                //tmpDB.open(true); // reopen it read only (no, do this in the read manager)
                // pass tmpDB to the reader thread to be opened ro, and managed
                if (readManager != null) {
                    //System.err.println("DBWriteManager: giving db to read manager.");
                    readManager.addDatabase(tmpDB);
                }
                
                
                // don't need this since we create a new one 
                // tmpDB.setIndex(nextIndex);
                
                // create new testdatabase for writing
                tmpDB = new TestDatabase(path + "/writeEnv", nextIndex);
                tmpDB.setUseEnvironment(useEnvironment);
                tmpDB.setUseSecondaries(useSecondaries);
                tmpDB.setPrimaryType(primaryType);
                ++nextIndex;
                tmpDB.open(false);
                readyLock.lock();
                ready.addLast(tmpDB);
                readyCondition.signal();
                readyLock.unlock();
            } catch (InterruptedException ex) {
                waitingLock.unlock();
                ex.printStackTrace();
            }
            runningLock.lock();
        }
        runningLock.unlock();
        
        while (!waiting.isEmpty()) {
            tmpDB = waiting.removeFirst();
            tmpDB.close();
        }
        while (!ready.isEmpty()) {
            tmpDB = ready.removeFirst();
            tmpDB.remove();
        }
    }
    
    public void finish() {
        runningLock.lock();
        running = false;
        runningLock.unlock();
        waitingLock.lock();
        waitingCondition.signal();
        waitingLock.unlock();
    }
    
    // public void closeDatabase(TestDatabase db)
    // put db in close queue
    // signal condition
    public void closeDatabase(TestDatabase db) {
        waitingLock.lock();
        waiting.addLast(db);
        waitingCondition.signal();
        waitingLock.unlock();
    }
    
    // public TestDatabase getNextDatabase()
    // return a database from the ready queue
    public TestDatabase getNextDatabase() {
        TestDatabase tmpDB;
        readyLock.lock();
        try {
            while (ready.isEmpty()) {
                
                readyCondition.await();
            }
            tmpDB = ready.removeFirst();
            return tmpDB;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            readyLock.unlock();
        }
    }
}