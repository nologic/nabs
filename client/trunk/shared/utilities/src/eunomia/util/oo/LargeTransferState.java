/*
 * LargeTransfer.java
 *
 * Created on January 31, 2007, 10:13 PM
 *
 */

package eunomia.util.oo;

import java.io.IOException;

/**
 *
 * @author Mikhail Sosonkin
 */
class LargeTransferState implements Comparable {
    private static int SEND_BYTES = 16*1024;
    
    private byte[] buffer;
    private LargeTransfer trans;
    private boolean isReadDone;
    private boolean isWriteDone;
    private Object assiciation;
    
    LargeTransferState(LargeTransfer lt, Object o) {
        isReadDone = false;
        isWriteDone = false;
        buffer = new byte[4096];
        trans = lt;
        assiciation = o;
    }
    
    LargeTransfer getTransfer() {
        return trans;
    }
    
    boolean isWriteDone() throws IOException {
        return isWriteDone;
    }
    
    boolean isReadDone() throws IOException {
        return isReadDone;
    }
    
    boolean isWritePortionReady() throws IOException {
        int avail = trans.getInputStream().available();
        return avail != -1; // StreamPipe is not ready but not closed.
    }
    
    void writePortion(NabObjectOutput out) throws IOException{
        if(out.write(trans.getInputStream(), Integer.MAX_VALUE, true) == 0) {
            trans.getInputStream().close();
            isWriteDone = true;
        }
    }
    
    void readPortion(NabObjectInput in) throws IOException {
        int avail = in.readInt();
        int totalRead = 0;
        int read;
        
        if(avail == 0){
            isReadDone = true;
            trans.receiveCompleted();
            return;
        }

        while(totalRead < avail) {
            int diff = avail - totalRead;
            
            if(diff == 0) {
                return;
            } else if(diff < buffer.length) {
                in.read(buffer, 0, diff);
                trans.getOutputStream().write(buffer, 0, diff);
                return;
            } else {
                read = in.read(buffer);
                trans.getOutputStream().write(buffer, 0, read);
                totalRead += read;
            }
        }
    }

    public int compareTo(Object o) {
        if(hashCode() > o.hashCode()) {
            return -1;
        } else if(hashCode() < o.hashCode()) {
            return 1;
        } else {
            return 0;
        }
    }

    Object getAssiciation() {
        return assiciation;
    }
}