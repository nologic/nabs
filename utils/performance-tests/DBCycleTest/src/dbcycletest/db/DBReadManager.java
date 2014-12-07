package dbcycletest.db;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.StatsConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Justin Stallard
 */
public class DBReadManager extends Thread {
    private static final int MAX_QUEUE_SIZE = 100;
    
    private boolean running;
    private Lock runningLock;
    
    private Environment readEnv;
    
    // queue of ready dbs (those ready to read)
    private LinkedList<TestDatabase> ready;
    private Lock readyLock;
    private Condition readyCondition;
    
    // queue of waiting dbs (those waiting to be opened ro)
    private LinkedList<TestDatabase> waiting;
    private Lock waitingLock;
    private Condition waitingCondition;
    
    
    public DBReadManager() {
        readEnv = null;
        
        ready = new LinkedList<TestDatabase>();
        waiting = new LinkedList<TestDatabase>();
        
        readyLock = new ReentrantLock();
        readyCondition = readyLock.newCondition();
        waitingLock = new ReentrantLock();
        waitingCondition = waitingLock.newCondition();
        
        runningLock = new ReentrantLock();
    }
    
    public boolean openEnvironment(String envPath, long cacheSize) {
        runningLock.lock();
        if (running) {
            runningLock.unlock();
            return false;
        }
        runningLock.unlock();
        
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        envConfig.setCacheSize(cacheSize);
        envConfig.setInitializeCache(true);
        envConfig.setTransactional(false);
        envConfig.setInitializeLogging(false);
        envConfig.setInitializeLocking(false);
        envConfig.setThreaded(false);
        envConfig.setPrivate(true);
        File envFile = new File(envPath);
        if (!envFile.exists()) {
            envFile.mkdir();
        }
        try {
            readEnv = new Environment(envFile, envConfig);
            System.err.println("DBReadManager: Opened environment. Mutex stats:");
            System.err.println("DBReadManager: Environment is " + (readEnv.getConfig().getInitializeCDB()?"":"not") + " configured for CDB");
            System.err.println("DBReadManager: Environment is " + (readEnv.getConfig().getCDBLockAllDatabases()?"":"not") + " configured for CDBLockAllDBs");
            System.err.println("DBReadManager: Environment uses system memory for cache: " + (readEnv.getConfig().getSystemMemory()?"yes":"no"));
            System.err.println("DBReadManager: Environment is private: " + (readEnv.getConfig().getPrivate()?"yes":"no"));
            System.err.println(readEnv.getMutexStats(StatsConfig.DEFAULT).toString());
            //System.err.println(readEnv.getLockStats(StatsConfig.DEFAULT).toString());
            return true;
        } catch (DatabaseException e) {
            e.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public void run() {
        TestDatabase tmpDB;
        //System.err.println("DBReadManager: Waiging for running lock...");
        runningLock.lock();
        //System.err.println("DBReadManager: Running lock acquired.");
        running = true;
        while (running) {
            runningLock.unlock();
            //System.err.println("DBReadManager: Running...waiting for waitingLock");
            waitingLock.lock();
            //System.err.println("DBReadManager: Waiting lock acquired.");
            try {
                while (waiting.isEmpty()) {
                    //System.err.println("DBReadManager: Waiting for signal...");
                    waitingCondition.await();
                    //System.err.println("DBReadManager: Got signal! Let's go.");
                }
                //System.err.println("DBReadManager: Something's in the waiting queue. Lat's take care of it.");
                runningLock.lock();
                // FIXME? is this in the wrong place? maybe it belongs in the
                // above loop?
                if (!running) {
                    break;
                }
                runningLock.unlock();
                tmpDB = waiting.removeFirst();
                waitingLock.unlock();
                
                if (readEnv != null) {
                    //System.err.println("DBReadManager: readEnv != null...moving db.");
                    try {
                        if (!tmpDB.moveTo(readEnv.getHome().getAbsolutePath())) {
                            System.err.println("Error moving database files! Time to bail.");
                            System.exit(1);
                        }
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                    tmpDB.setEnvironment(readEnv);
                } else {
                    System.err.println("DBReadManager: readEnv == null...what's going on?");
                }
                if (!tmpDB.open(true)) {
                    System.err.println("DBReadManager: Unable to open DB! Exiting...");
                    System.exit(1);
                }
                try {
                    System.err.println(readEnv.getMutexStats(StatsConfig.DEFAULT).toString());
                } catch (DatabaseException e) {
                    System.err.println("Unable to print mutex stats!");
                    e.printStackTrace();
                    System.exit(1);
                }
                /*
                try {
                    System.err.println(readEnv.getLockStats(StatsConfig.DEFAULT).toString());
                } catch (DatabaseException e) {
                    System.err.println("Unable to print lock stats!");
                    e.printStackTrace();
                    System.exit(1);
                }
                 * */
                readyLock.lock();
                ready.addLast(tmpDB);
                
                if (ready.size() > MAX_QUEUE_SIZE) {
                    tmpDB = ready.removeFirst();
                } else {
                    tmpDB = null;
                }
                System.err.println("readManager ready queue size: " + ready.size());
                readyCondition.signal();
                readyLock.unlock();
                
                if (tmpDB != null) {
                    tmpDB.getWriteLock();
                    if (!tmpDB.close()) {
                        System.err.println("DBReadManager: Error closing old DB! Exiting...");
                        System.exit(1);
                    }
                }
            } catch (InterruptedException ex) {
                waitingLock.unlock();
                ex.printStackTrace();
            }
            runningLock.lock();
        }
        runningLock.unlock();
        
        while (!waiting.isEmpty()) {
            waiting.removeFirst();
        }
        while (!ready.isEmpty()) {
            tmpDB = ready.removeFirst();
            tmpDB.close();
        }
        
        if (readEnv != null) {
            try {
                readEnv.close();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
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
    
    // adds a new database to the queue to be opened 
    public void addDatabase(TestDatabase db) {
        waitingLock.lock();
        waiting.addLast(db);
        waitingCondition.signal();
        waitingLock.unlock();
        //System.err.println("DBReadManager: New DB added to waiting queue.");
    }
    
    public int getFirstDatabaseIndex() {
        readyLock.lock();
        try {
            while (ready.isEmpty()) {
                readyCondition.await();
            }
            
            if (ready.size() > MAX_QUEUE_SIZE) {
                return ready.size() - MAX_QUEUE_SIZE;
            } else {
                return 0;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        } finally {
            readyLock.unlock();
        }
    }
    
    // returns the TestDatabase at position index in the ready List
    // if the ready list is empty, wait for it not to be
    // if index is out of bounds, returns null
    public TestDatabase getDatabase(int index) {
        readyLock.lock();
        try {
            while (ready.isEmpty()) {
                readyCondition.await();
            }
            if (index >= ready.size() || index < ready.size() - MAX_QUEUE_SIZE) {
                // only return DBs within the past 100M insertions
                return null;
            }
            return ready.get(index);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            readyLock.unlock();
        }
    }
}