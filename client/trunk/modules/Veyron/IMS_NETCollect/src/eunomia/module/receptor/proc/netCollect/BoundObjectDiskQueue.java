/*
 * BoundObjectDiskQueue.java
 *
 * Created on August 16, 2008, 1:54 PM
 *
 */

package eunomia.module.receptor.proc.netCollect;

import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.bind.BoundObject;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Mikhail Sosonkin
 */
public class BoundObjectDiskQueue {
    private int maxMemEntries;
    private BoundObject bObj;
    
    private ArrayList archList;
    private BFile curFile;
    private byte[] buff;
    private byte[] read_buff;
    private LinkedList fileQueue;
    private ReentrantLock curLock;
    private ReentrantLock archLock;
    private long archCount;
    
    public BoundObjectDiskQueue(int maxMemEntries, BoundObject sample) {
        this.maxMemEntries = maxMemEntries;
        bObj = sample;
        
        archList = new ArrayList(maxMemEntries);
        archLock = new ReentrantLock(true);
        curLock = new ReentrantLock();
        fileQueue = new LinkedList();
        buff = new byte[bObj.getByteSize()];
        read_buff = new byte[bObj.getByteSize()];
    }
    
    public void putArch(BoundObject obj) {
        ++archCount;
        
        BFile put = getPutObject();
        if(put == null) {
            archList.add(obj);
        } else {
            OutputStream out = put.out;
            obj.serialize(buff, 0);
            
            try {
                out.write(buff);
                ++put.count;
                put.inUse = false;
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(0);
            }
        }
        
        /*archLock.lock();
        if(archList.size() < maxMemEntries) {
            
            archLock.unlock();
        } else {
            archLock.unlock();
            curFileLock.lock();
            
            BFile bfile = getBufferFile();

            
            curFileLock.unlock();
        }*/
    }
    
    // an empty array to return when no data is available.
    private BoundObject[] arrType;
    public BoundObject[] getNextArray() {
        if(arrType == null) {
            arrType = (BoundObject[]) Array.newInstance(bObj.getClass(), 0);
        }
        
        BoundObject[] arr = arrType;
       
        archLock.lock();
        
        if(archList.size() > 0) {
            arr = (BoundObject[]) archList.toArray(arrType);
            archList.clear();
        }

        if(fileQueue.size() > 0) {
            // prepare next if needed
            // read from file, delete file and return array.
            BFile bfile;
            
            bfile = (BFile)fileQueue.removeFirst();
            
            if(bfile == curFile) {
                bfile.requestClose = true;
                
                if(bfile.inUse) { 
                    while(!bfile.closed);
                } else {
                    curFile.closed = true;
                    try {
                        bfile.out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    curFile.out = null;
                }
            }
            
            try {
                InputStream fin = new BufferedInputStream(new FileInputStream(bfile.file), bObj.getByteSize() * 1000);
                while(fin.available() > 0) {
                    BoundObject c = bObj.clone();
                    fin.read(read_buff);
                    c.unserialize(read_buff, 0);
                    archList.add(c);
                }
                fin.close();

                if(!bfile.file.delete()) {
                    bfile.file.deleteOnExit();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        archLock.unlock();
        
        return arr;
    }
    
    private BFile getPutObject() {
        // Need to sync fileQueue
        if(archList.size() < maxMemEntries && fileQueue.size() == 0) {
            return null;
        }
        
        if(curFile == null || curFile.count >= maxMemEntries || 
           curFile.requestClose || curFile.closed) {
            // open new file.
            try {
                if(curFile != null && !curFile.closed) {
                    curFile.closed = true;
                    curFile.out.close();
                    curFile.out = null;
                }

                curFile = makeNewFile();
                curFile.inUse = true;
                
                fileQueue.addLast(curFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return curFile;
    }
    
    private BFile makeNewFile() throws FileNotFoundException, IOException {
        BFile nFile = new BFile();

        nFile.file = File.createTempFile("arch", "bnd");
        nFile.out = new BufferedOutputStream(new FileOutputStream(nFile.file));
        nFile.closed = false;
        nFile.requestClose = false;
        
        return nFile;
    }
    
    public long getQueueSize() {
        return archList.size();
    }
    
    private class BFile {
        public volatile boolean requestClose;
        public volatile boolean closed;
        public volatile boolean inUse;
        
        public volatile OutputStream out;
        public volatile File file;
        public volatile int count;
    }
}