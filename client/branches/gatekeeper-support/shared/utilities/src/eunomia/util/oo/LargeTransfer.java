/*
 * LargeTransfet.java
 *
 * Created on February 10, 2007, 6:22 PM
 *
 */

package eunomia.util.oo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class LargeTransfer implements Externalizable {
    private static AtomicInteger ID;
    
    private int id;
    private InputStream in;
    private OutputStream out;
    
    private boolean recvComplete;
    private boolean delFile;
    private File tmpFile;
    
    static {
        ID = new AtomicInteger(0);
    }
    
    public LargeTransfer() {
        recvComplete = false;
        id = ID.getAndIncrement();
    }

    public void setInputStream(InputStream in) {
        this.in = in;
    }
    
    public InputStream getInputStream() {
        return in;
    }
    
    public OutputStream getOutputStream() throws FileNotFoundException {
        if(out == null) {
            this.out = new BufferedOutputStream(new FileOutputStream(tmpFile));
        }
        return out;
    }
    
    public void setDestinationFile(File file) {
        tmpFile = file;
    }
    
    public File getDestinationFile() {
        return tmpFile;
    }
    
    public int getIdentifier() {
        return id;
    }
    
    public boolean isReceived() {
        return recvComplete;
    }
    
    public final void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
    }

    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        
        if(tmpFile != null) {
            delFile = false;
        } else {
            delFile = true;
            tmpFile = File.createTempFile("lt" + id, ".lt");
            tmpFile.deleteOnExit();
        }
    }
    
    final void receiveCompleted() throws IOException {
        out.flush();
        out.close();
        
        in = new BufferedInputStream(new FileInputStream(tmpFile));
        recvComplete = true;
    }
    
    final void sendCompleted() {
        try {
            in.close();
        } catch (IOException ex) {
        }
    }
}