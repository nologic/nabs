package dbcycletest;

import com.sleepycat.collections.StoredIterator;
import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredValueSet;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.SecondaryCursor;
import dbcycletest.db.DBReadManager;
import dbcycletest.db.TestDatabase;
import dbcycletest.db.bindings.FlowIDEntityBinding;
import dbcycletest.db.keys.FlowIDFlowIDKey;
import dbcycletest.db.keys.FlowIDHostKey;
import dbcycletest.db.keys.FlowIDTimeKey;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Justin Stallard
 */
public class TraversalThread extends Thread {
    
    private long overallStartTime;
    private long dbStartTime;
    private long thousandStartTime;
    private long thousandEndTime;
    
    private PrintStream primaryPrintStream;
    private PrintStream flowIDsByFlowIDPrintStream;
    private PrintStream flowIDsByHostPrintStream;
    private PrintStream flowIDsByEndTimePrintStream;
    private PrintStream flowIDsByExistTimePrintStream;

    private boolean running;
    private Lock runningLock;
    
    DBReadManager readManager;
    private TestDatabase curDB;
    private int curIndex;
    private boolean started;
    private boolean collections;
    
    public TraversalThread(DBReadManager readManager) {
        overallStartTime = -1;
        this.readManager = readManager;
        
        started = false;
        running = true;
        curIndex = 0;
        collections = false;
        
        primaryPrintStream = null;
        flowIDsByFlowIDPrintStream = null;
        flowIDsByHostPrintStream = null;
        flowIDsByEndTimePrintStream = null;
        flowIDsByExistTimePrintStream = null;
        
        runningLock = new ReentrantLock();
    }
    
    // only set if thread hasn't been started
    // returns true if set, false otherwise
    public boolean setCollection(boolean collections) {
        if (!started) {
            this.collections = collections;
        }
        return (!started);
    }
    
    public boolean setOverallStartTime(long overallStartTime) {
        if (!started) {
            this.overallStartTime = overallStartTime;
        }
        return (!started);
    }
    
    public boolean setPrimaryPrintStream(PrintStream primaryPrintStream) {
        if (!started) {
            this.primaryPrintStream = primaryPrintStream;
        }
        return (!started);
    }
    
    public boolean setFlowIDsByFlowIDPrintStream(PrintStream flowIDsByFlowIDPrintStream) {
        if (!started) {
            this.flowIDsByFlowIDPrintStream = flowIDsByFlowIDPrintStream;
        }
        return (!started);
    }
    
    public boolean setFlowIDsByHostPrintStream(PrintStream flowIDsByHostPrintStream) {
        if (!started) {
            this.flowIDsByHostPrintStream = flowIDsByHostPrintStream;
        }
        return (!started);
    }
    
    public boolean setFlowIDsByEndTimePrintStream(PrintStream flowIDsByEndTimePrintStream) {
        if (!started) {
            this.flowIDsByEndTimePrintStream = flowIDsByEndTimePrintStream;
        }
        return (!started);
    }
    
    public boolean setFlowIDsByExistTimePrintStream(PrintStream flowIDsByExistTimePrintStream) {
        if (!started) {
            this.flowIDsByExistTimePrintStream = flowIDsByExistTimePrintStream;
        }
        return (!started);
    }
    
    @Override
    public void run() {
        started = true;
        runningLock.lock();
runloop:
        while (running) {
            runningLock.unlock();
            // get a db from the read manager
            curDB = readManager.getDatabase(curIndex);
            while (curDB == null) {
                curIndex = readManager.getFirstDatabaseIndex();
                runningLock.lock();
                continue runloop;
            }
            
            // acquire the read lock for this db. if not possible, get the next DB
            if (!curDB.tryReadLock()) {
                ++curIndex;
                continue runloop;
            }
            
            // traverse the databases
            if (collections) {
                traversePrimaryCollections();
                //traverseSecondariesCollections();
            } else {
                traversePrimaryCursor();
                traverseSecondariesCursor();
            }
            
            // release the read lock
            curDB.releaseReadLock();
            
            // incrememnt curIndex
            ++curIndex;
            runningLock.lock();
        }
        runningLock.unlock();
    }
    
    public void finish() {
        runningLock.lock();
        running = false;
        runningLock.unlock();
    }
    
    private void traversePrimaryCollections() {
        StoredValueSet valueSet = curDB.getPrimaryValueSet(true);
        StoredIterator valueIter = valueSet.storedIterator();
        FlowID flowID;
        int count = 0;
        
        System.err.println("Traversing primary collection....");
        System.err.println("index: " + curIndex + '\n');
        
        dbStartTime = thousandStartTime = System.currentTimeMillis();
        while (valueIter.hasNext()) {
            flowID = (FlowID) valueIter.next();
            flowID.getSourceIP();
            ++count;
            if (count % 1000 == 0 && primaryPrintStream != null) {
                // print stats
                thousandEndTime = System.currentTimeMillis();
                primaryPrintStream.println((count - 1000) + " " + ((double) thousandEndTime - thousandStartTime) / 1000 + " " + ((double) thousandStartTime - overallStartTime) / 1000);
                thousandStartTime = thousandEndTime;
            }
        }
        thousandEndTime = System.currentTimeMillis();
        System.err.println("Finished traversing primary collection.");
        System.err.println("Records traversed: " + count + " Took: " + ((double) thousandEndTime - dbStartTime) / 1000 + " seconds");
        
        valueIter.close();
    }
    
    private void traverseSecondariesCollections() {
        StoredMap dbMap = curDB.getFlowIDsByFlowIDMap();
        StoredKeySet keySet = (StoredKeySet) dbMap.keySet();
        //StoredKeySet keySet = curDB.getFlowIDsByFlowIDKeySet();

        //StoredValueSet valueSet = (StoredValueSet) dbMap.values();
        //StoredIterator valueIter = valueSet.storedIterator();
        StoredIterator keyIter = keySet.storedIterator();
        FlowID flowID;
        int keyCount = 0;
        int recordCount = 0;
        
        System.err.println("Traversing secondary collection (FlowIDsByFlowID)....");
        System.err.println("index: " + curIndex + '\n');
        
        //while (valueIter.hasNext()) {
        //    flowID = (FlowID) valueIter.next();
        Collection dups;
        Iterator dupsIter;
        FlowIDFlowIDKey flowIDKey;
        
        dbStartTime = thousandStartTime = System.currentTimeMillis();
        while (keyIter.hasNext()) {
            flowIDKey = (FlowIDFlowIDKey) keyIter.next();
            //flowID = (FlowID) dbMap.get(flowIDKey);
            dups = dbMap.duplicates(flowIDKey);
            dupsIter = dups.iterator();
            while (dupsIter.hasNext()) {
                flowID = (FlowID) dupsIter.next();
                flowID.getSourceIP();
                ++recordCount;
                
                if (recordCount % 1000 == 0 && flowIDsByFlowIDPrintStream != null) {
                    // print stats
                    thousandEndTime = System.currentTimeMillis();
                    flowIDsByFlowIDPrintStream.println((recordCount - 1000) + " " + ((double) thousandEndTime - thousandStartTime) / 1000 + " " + ((double) thousandStartTime - overallStartTime) / 1000);
                    thousandStartTime = thousandEndTime;
                }
            }
            ++keyCount;
        }
        thousandEndTime = System.currentTimeMillis();
        System.err.println("Finished traversing secondary collection (FlowIDsByFlowID).");
        System.err.println("Keys: " + keyCount + " Records traversed: " + recordCount + " Took: " + ((double) thousandEndTime - dbStartTime) / 1000 + " seconds");
        
        // valueIter.close();
        keyIter.close();
        
        
        dbMap = curDB.getFlowIDsByHostMap();
        //valueSet = (StoredValueSet) dbMap.values();
        //valueIter = valueSet.storedIterator();
        keySet = (StoredKeySet) dbMap.keySet();
        keyIter = keySet.storedIterator();
        
        keyCount = recordCount = 0;
        
        System.err.println("Traversing secondary collection (FlowIDsByHost)....");
        System.err.println("index: " + curIndex + '\n');
        
        //while (valueIter.hasNext()) {
        //    flowID = (FlowID) valueIter.next();
        FlowIDHostKey flowIDHostKey;
        dbStartTime = thousandStartTime = System.currentTimeMillis();
        while (keyIter.hasNext()) {
            flowIDHostKey = (FlowIDHostKey) keyIter.next();
            dups = dbMap.duplicates(flowIDHostKey);
            dupsIter = dups.iterator();
            while (dupsIter.hasNext()) {
                flowID = (FlowID) dupsIter.next();
                flowID.getSourceIP();
                ++recordCount;
                if (recordCount % 1000 == 0 && flowIDsByHostPrintStream != null) {
                    // print stats
                    thousandEndTime = System.currentTimeMillis();
                    flowIDsByHostPrintStream.println((recordCount - 1000) + " " + ((double) thousandEndTime - thousandStartTime) / 1000 + " " + ((double) thousandStartTime - overallStartTime) / 1000);
                    thousandStartTime = thousandEndTime;
                }
            }
            ++keyCount;
        //    flowID.getSourceIP();
        //    ++count;
        }
        thousandEndTime = System.currentTimeMillis();
        System.err.println("Finished traversing secondary collection (FlowIDsByHost).");
        System.err.println("Keys: " + keyCount + " Records traversed: " + recordCount + " Took: " + ((double) thousandEndTime - dbStartTime) / 1000 + " seconds");
        
        //valueIter.close();
        keyIter.close();
        
        
        dbMap = curDB.getFlowIDsByEndTimeMap();
        //valueSet = (StoredValueSet) dbMap.values();
        //valueIter = valueSet.storedIterator();
        keySet = (StoredKeySet) dbMap.keySet();
        keyIter = keySet.storedIterator();
        
        keyCount = recordCount = 0;
        
        System.err.println("Traversing secondary collection (FlowIDsByEndTime)....");
        System.err.println("index: " + curIndex + '\n');
        
        //while (valueIter.hasNext()) {
            //flowID = (FlowID) valueIter.next();
        FlowIDTimeKey flowIDTimeKey;
        dbStartTime = thousandStartTime = System.currentTimeMillis();
        while (keyIter.hasNext()) {
            flowIDTimeKey = (FlowIDTimeKey) keyIter.next();
            //flowID = (FlowID) dbMap.get(flowIDKey);
            dups = dbMap.duplicates(flowIDTimeKey);
            dupsIter = dups.iterator();
            while (dupsIter.hasNext()) {
                flowID = (FlowID) dupsIter.next();
                flowID.getSourceIP();
                ++recordCount;
                if (recordCount % 1000 == 0 && flowIDsByEndTimePrintStream != null) {
                    // print stats
                    thousandEndTime = System.currentTimeMillis();
                    flowIDsByEndTimePrintStream.println((recordCount - 1000) + " " + ((double) thousandEndTime - thousandStartTime) / 1000 + " " + ((double) thousandStartTime - overallStartTime) / 1000);
                    thousandStartTime = thousandEndTime;
                }
            }
            ++keyCount;
            //flowID.getSourceIP();
            //++count;
        }
        thousandEndTime = System.currentTimeMillis();
        System.err.println("Finished traversing secondary collection (FlowIDsByEndTime).");
        System.err.println("Keys: " + keyCount + " Records traversed: " + recordCount + " Took: " + ((double) thousandEndTime - dbStartTime) / 1000 + " seconds");
        
        //valueIter.close();
        keyIter.close();
        
        
        dbMap = curDB.getFlowIDsByExistTimeMap();
        //valueSet = (StoredValueSet) dbMap.values();
        //valueIter = valueSet.storedIterator();
        keySet = (StoredKeySet) dbMap.keySet();
        keyIter = keySet.storedIterator();
        
        keyCount = recordCount = 0;
        
        System.err.println("Traversing secondary collection (FlowIDsByExistTime)....");
        System.err.println("index: " + curIndex + '\n');
        
        //while (valueIter.hasNext()) {
        //    flowID = (FlowID) valueIter.next();
        dbStartTime = thousandStartTime = System.currentTimeMillis();
        while (keyIter.hasNext()) {
            flowIDTimeKey = (FlowIDTimeKey) keyIter.next();
            dups = dbMap.duplicates(flowIDTimeKey);
            dupsIter = dups.iterator();
            while (dupsIter.hasNext()) {
                flowID = (FlowID) dupsIter.next();
                flowID.getSourceIP();
                ++recordCount;
                if (recordCount % 1000 == 0 && flowIDsByExistTimePrintStream != null) {
                    // print stats
                    thousandEndTime = System.currentTimeMillis();
                    flowIDsByExistTimePrintStream.println((recordCount - 1000) + " " + ((double) thousandEndTime - thousandStartTime) / 1000 + " " + ((double) thousandStartTime - overallStartTime) / 1000);
                    thousandStartTime = thousandEndTime;
                }
            }
            ++keyCount;
            //flowID.getSourceIP();
            //++count;
        }
        thousandEndTime = System.currentTimeMillis();
        System.err.println("Finished traversing secondary collection (FlowIDsByExistTime).");
        System.err.println("Keys: " + keyCount + " Records traversed: " + recordCount + " Took: " + ((double) thousandEndTime - dbStartTime) / 1000 + " seconds");
        
        //valueIter.close();
        keyIter.close();
    }
    
    private void traversePrimaryCursor() {
        try {
            Cursor cursor = curDB.getPrimaryCursor(true);

            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();

            FlowID flowID;
            FlowIDEntityBinding flowBinding = new FlowIDEntityBinding();
            int count = 0;
            
            System.err.println("Traversing primary....");
            System.err.println("index: " + curIndex + '\n');
            
            while (cursor.getNext(key, data, null) == OperationStatus.SUCCESS) {
                flowID = (FlowID) flowBinding.entryToObject(key, data);
                flowID.getSourceIP();
                ++count;
            }
            System.err.println("Finished traversing primary.");
            System.err.println("Records traversed: " + count);
            cursor.close();
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
    
    private void traverseSecondariesCursor() {
        try {
            DatabaseEntry key = new DatabaseEntry();
            DatabaseEntry pKey = new DatabaseEntry();
            DatabaseEntry data = new DatabaseEntry();

            FlowID flowID;
            FlowIDEntityBinding flowBinding = new FlowIDEntityBinding();
            int count = 0;

            SecondaryCursor cursor = curDB.getFlowIDsByFlowIDCursor();

            System.err.println("Traversing secondary (FlowIDsByFlowID)...");
            System.err.println("index: " + curIndex + '\n');

            cursor.getFirst(key, pKey, data, null);
            flowID = (FlowID) flowBinding.entryToObject(pKey, data);
            flowID.getSourceIP();
            ++count;
            
            while (cursor.getNextDup(key, pKey, data, null) == OperationStatus.SUCCESS) {
                flowID = (FlowID) flowBinding.entryToObject(pKey, data);
                flowID.getSourceIP();
                ++count;
            }
            System.err.println("Finished traversing secondary (FlowIDsByFlowID).");
            System.err.println("Records traversed: " + count);
            cursor.close();
            
            count = 0;
            
            cursor = curDB.getFlowIDsByHostCursor();

            System.err.println("Traversing secondary (FlowIDsByHost)...");
            System.err.println("index: " + curIndex + '\n');
            
            cursor.getFirst(key, pKey, data, null);
            flowID = (FlowID) flowBinding.entryToObject(pKey, data);
            flowID.getSourceIP();
            ++count;

            while (cursor.getNextDup(key, pKey, data, null) == OperationStatus.SUCCESS) {
                flowID = (FlowID) flowBinding.entryToObject(pKey, data);
                flowID.getSourceIP();
                ++count;
            }
            System.err.println("Finished traversing secondary (FlowIDsByHost).");
            System.err.println("Records traversed: " + count);
            
            count = 0;
            
            cursor = curDB.getFlowIDsByEndTimeCursor();

            System.err.println("Traversing secondary (FlowIDsByEndTime)...");
            System.err.println("index: " + curIndex + '\n');

            cursor.getFirst(key, pKey, data, null);
            flowID = (FlowID) flowBinding.entryToObject(pKey, data);
            flowID.getSourceIP();
            ++count;
            
            while (cursor.getNextDup(key, pKey, data, null) == OperationStatus.SUCCESS) {
                flowID = (FlowID) flowBinding.entryToObject(pKey, data);
                flowID.getSourceIP();
                ++count;
            }
            System.err.println("Finished traversing secondary (FlowIDsByEndTime).");
            System.err.println("Records traversed: " + count);
            
            count = 0;
            
            cursor = curDB.getFlowIDsByExistTimeCursor();

            System.err.println("Traversing secondary (FlowIDsByExistTime)...");
            System.err.println("index: " + curIndex + '\n');

            cursor.getFirst(key, pKey, data, null);
            flowID = (FlowID) flowBinding.entryToObject(pKey, data);
            flowID.getSourceIP();
            ++count;
            
            while (cursor.getNextDup(key, pKey, data, null) == OperationStatus.SUCCESS) {
                flowID = (FlowID) flowBinding.entryToObject(pKey, data);
                flowID.getSourceIP();
                ++count;
            }
            System.err.println("Finished traversing secondary (FlowIDsByExistTime).");
            System.err.println("Records traversed: " + count);
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
}