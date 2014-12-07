/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dbcycletest;

import com.sleepycat.db.DatabaseType;
import dbcycletest.db.DBWriteManager;
import dbcycletest.db.DBReadManager;
import dbcycletest.db.TestDatabase;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author justin
 */
public class Main {
    private static final String DB_PATH = "/Users/justin/testenv";
    
    private static DBWriteManager writeManager;
    private static DBReadManager readManager;
    private static TraversalThread traversalThread;
    private static long overallStartTime;
    private static long thousandStartTime;
    private static long thousandEndTime;
    //private static long transitionStartTime;
    //private static long transitionEndTime;
    private static int flowIndex;
    
    private static FileOutputStream primaryOutputStream;
    private static PrintStream primaryPrintStream;
    private static FileOutputStream flowIDOutputStream;
    private static PrintStream flowIDPrintStream;
    private static FileOutputStream hostOutputStream;
    private static PrintStream hostPrintStream;
    private static FileOutputStream endTimeOutputStream;
    private static PrintStream endTimePrintStream;
    private static FileOutputStream existTimeOutputStream;
    private static PrintStream existTimePrintStream;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        overallStartTime = System.currentTimeMillis();
        
        // start read database manager
        readManager = new DBReadManager();
        readManager.setName("DBReadManager");
        readManager.openEnvironment(DB_PATH + File.separator + "readEnv", 256*1024*1024);
        readManager.start();
        
        // start write database manager
        writeManager = new DBWriteManager(DB_PATH, 2, readManager, false, true);
        writeManager.setName("DBWriteManager");
        writeManager.setPrimaryType(DatabaseType.BTREE);
        writeManager.start();
        
        // set up print streams for traversal thread
        try {
            primaryOutputStream = new FileOutputStream("primaryTraversalStats");
            primaryPrintStream = new PrintStream(primaryOutputStream, true);
            flowIDOutputStream = new FileOutputStream("flowIDTraversalStats");
            flowIDPrintStream = new PrintStream(flowIDOutputStream, true);
            hostOutputStream = new FileOutputStream("hostTraversalStats");
            hostPrintStream = new PrintStream(hostOutputStream, true);
            endTimeOutputStream = new FileOutputStream("endTimeTraversalStats");
            endTimePrintStream = new PrintStream(endTimeOutputStream, true);
            existTimeOutputStream = new FileOutputStream("existTimeTraversalStats");
            existTimePrintStream = new PrintStream(existTimeOutputStream, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Unable to open stats output file. Exiting...");
            System.exit(1);
        }
        
        // start traversal thread
        traversalThread = new TraversalThread(readManager);
        traversalThread.setCollection(true);
        traversalThread.setName("TraversalThread");
        traversalThread.setOverallStartTime(overallStartTime);
        traversalThread.setPrimaryPrintStream(primaryPrintStream);
        traversalThread.setFlowIDsByFlowIDPrintStream(flowIDPrintStream);
        traversalThread.setFlowIDsByHostPrintStream(hostPrintStream);
        traversalThread.setFlowIDsByEndTimePrintStream(endTimePrintStream);
        traversalThread.setFlowIDsByExistTimePrintStream(existTimePrintStream);
        traversalThread.start();
        
        System.err.println("All threads created. Took " + ((double) System.currentTimeMillis() - overallStartTime) / 1000);
        
        
        FlowID id = new FlowID();
        TestDatabase db = writeManager.getNextDatabase();
        
        
        int maxDBSize = 1000000;
        int maxRecords = 300000000;
        flowIndex = 1;
        while (flowIndex <= maxRecords) {
            // use a new database every 1000000 insertions
            if (flowIndex > 1 && (flowIndex - 1) % maxDBSize == 0) {
                //transitionStartTime = System.currentTimeMillis();
                writeManager.closeDatabase(db);
                db = writeManager.getNextDatabase();
                //transitionEndTime = System.currentTimeMillis();
                //System.err.println("DB Transition took " + ((double) (transitionEndTime - transitionStartTime)) / 1000 + " seconds");
            }
            
            //if ((flowIndex - 1) % 1000 == 0) { // -1 because flowIndex starts @ 1
            thousandStartTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; ++i) {
                id.setKey(((flowIndex - 1) % maxDBSize) + 1);
                id.makeRandom();
                if (!db.add(id)) {
                    System.err.println("Error writing to DB! Exiting...");
                    if (!db.close()) {
                        System.err.println("Error closing DB!");
                    }
                    System.exit(1);
                }
                ++flowIndex;
            }

            thousandEndTime = System.currentTimeMillis();

            //System.out.println("Time to insert 1000 flows with " + (flowIndex - 1000) + " flows in the db: " + ((double) (thousandEndTime - thousandStartTime)) / 1000 + " seconds");
            System.out.println((flowIndex - 1001) + " " + ((double) (thousandEndTime - thousandStartTime)) / 1000 + " " + ((double) thousandStartTime - overallStartTime) / 1000);
            /*    
                continue;
            }
            */
            /*
            id.setKey(flowIndex);
            id.makeRandom();
            if (!db.add(id)) {
                System.err.println("Error writing to DB! Exiting...");
                if (!db.close()) {
                    System.err.println("Error closing DB!");
                }
                System.exit(1);
            }

            ++flowIndex;
            */
        }
        
        traversalThread.finish();
        try {
            traversalThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        writeManager.closeDatabase(db);
        writeManager.finish();
        readManager.finish();
        try {
            writeManager.join();
            readManager.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
