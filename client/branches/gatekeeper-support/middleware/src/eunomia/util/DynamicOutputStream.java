/*
 * DynamicOutputStream.java
 *
 * Created on April 1, 2007, 1:29 PM
 *
 */

package eunomia.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class DynamicOutputStream extends OutputStream {
    private OutputStream curOut;
    private ByteArrayOutputStream primary;
    private int switchSize;
    private int wrote;
    private byte[] buff;
    
    public DynamicOutputStream(ByteArrayOutputStream primary, int switchSize) {
        this.primary = primary;
        this.switchSize = switchSize;
        curOut = primary;
    }

    public void write(int b) throws IOException {
        if(buff == null) {
            buff = new byte[1];
        }
        
        buff[0] = (byte)(b & 0xFF);
        write(buff);
    }
    
    public void close() throws IOException {
        curOut.close();
    }
    
    public void write(byte b[], int off, int len) throws IOException {
	if (b == null) {
	    throw new NullPointerException();
	} else if ((off < 0) || (off > b.length) || (len < 0) ||
		   ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}

        if(switchSize != -1 && wrote + len > switchSize) {
            curOut = getSecondary();
            primary.writeTo(curOut);
            switchSize = -1;
        }
        
        curOut.write(b, off, len);
        wrote += len;
    }
    
    public abstract OutputStream getSecondary() throws IOException;
}