package eunomia.module.receptor.proc.dnsCollect;

import eunomia.module.receptor.libb.imsCore.dns.DNS;
import eunomia.module.receptor.libb.imsCore.dns.DNSFlowKey;
import eunomia.module.receptor.libb.imsCore.dns.DNSFlowRecord;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Justin Stallard
 */
public class WriteBuffer implements Runnable {
    private static final int BUF_SIZE = 1000; // buffer 1000 records before writing
    
    private int buffCount;
    
    private RecordBuffer curBuf;
    private Lock curBufLock;
    private int curKey;
    
    private LinkedList<RecordBuffer> waiting;
    private Lock waitingLock;
    private Condition waitingCondition;
    
    private LinkedList<RecordBuffer> ready;
    private Lock readyLock;
    private Condition readyCondition;

    private DNS dns;
    
    public WriteBuffer(DNS dns, int buffCount) {
        this.dns = dns;
        this.buffCount = buffCount;
        
        curBuf = new RecordBuffer();
        curKey = 0;
        
        waiting = new LinkedList<RecordBuffer>();
        ready = new LinkedList<RecordBuffer>();
        
        for (int i = 1; i < buffCount; ++i) {
            ready.addLast(new RecordBuffer());
        }
        
        curBufLock = new ReentrantLock();
        waitingLock = new ReentrantLock();
        readyLock = new ReentrantLock();
        
        waitingCondition = waitingLock.newCondition();
        readyCondition = readyLock.newCondition();
        
        Thread thread = new Thread(this);
        thread.setName("DNSCollect_WriteBuffer");
        thread.start();
    }

    public void run() {
        System.err.println("WriteBuffer thread started.");
        RecordBuffer work;
        while (true) {
            waitingLock.lock();
            try {
                while (waiting.isEmpty()) {
                    waitingCondition.await();
                }
                work = waiting.removeFirst();
                waitingLock.unlock();

                dns.addRecords(work.getBuff(), work.getBuffFill());
                if (work.timeToRoll()) {
                    dns.rollDB();
                }
                work.reset();
                
                readyLock.lock();
                ready.addLast(work);
                readyCondition.signal();
                readyLock.unlock();
            } catch (InterruptedException ex) {
                waitingLock.unlock();
                ex.printStackTrace();
            }
        }
    }
    
    public boolean addRecord(DNSFlowRecord record) {
        curBufLock.lock();
        if (!curBuf.addRecord(record, curKey)) {
            if (curBuf.timeToRoll()) {
                curKey = 0;
            }
            waitingLock.lock();
            waiting.addLast(curBuf);
            waitingCondition.signal();
            waitingLock.unlock();
            readyLock.lock();
            try {
                while (ready.isEmpty()) {
                    readyCondition.await();
                }
                curBuf = ready.removeFirst();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return false;
            } finally {
                readyLock.unlock();
            }
            if (!curBuf.addRecord(record, curKey)) {
                System.err.println("DNSCollect: WriteBuffer: addRecord(): Major problem!");
            }
        } else {
            ++curKey;
        }
        curBufLock.unlock();
        return true;
    }
    
    private class RecordBuffer {
        DNSFlowRecord[] buff;
        int curPos;
        long lastTimeSlice;
        boolean timeToRoll;
        
        public RecordBuffer() {
            buff = new DNSFlowRecord[BUF_SIZE];
            curPos = 0;
            lastTimeSlice = 0;
            timeToRoll = false;
        }
        
        public void reset() {
            curPos = 0;
            lastTimeSlice = 0;
            timeToRoll = false;
        }
        
        public boolean addRecord(DNSFlowRecord record, int key) {
            if (curPos != 0 && lastTimeSlice < record.getTimeSlice() && record.getTimeSlice() != 0 && lastTimeSlice != 0) {
                timeToRoll = true;
                System.err.println("DNSCollect: RecordBuffer: time to roll! ****************************");
                System.err.println("DNSCollect: RecordBuffer: old slice: " + lastTimeSlice + " new slice: " + record.getTimeSlice());
                return false;
            }
            
            if (curPos < buff.length) {
                ((DNSFlowKey) record.getKey()).setKey(key);
                buff[curPos] = record;
                ++curPos;
                if (record.getTimeSlice() != 0 && record.getTimeSlice() >= lastTimeSlice) {
                    lastTimeSlice = record.getTimeSlice();
                }
                return true;
            }
            
            return false;
        }
        
        public DNSFlowRecord[] getBuff() {
            return buff;
        }
        
        public int getBuffFill() {
            return curPos;
        }
        
        public boolean timeToRoll() {
            return timeToRoll;
        }
    }
}