/*
 * Pipe.java
 *
 * Created on February 20, 2007, 10:41 PM
 *
 */

package eunomia.util.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.Pipe;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StreamPipe extends InputStream {
    private static final int b_size = 4096;
    
    private OutputStream out;
    private Pipe pipe;
    private SourceChannel source;
    private Selector readSel;
    private ByteBuffer readBuff;
    private boolean isClosed;
    
    public StreamPipe() throws IOException {
        readBuff = ByteBuffer.allocateDirect(b_size);
        
        pipe = Pipe.open();
        readSel = Selector.open();
        
        source = pipe.source();
        source.configureBlocking(false);
        source.register(readSel, source.validOps());
        
        out = new OutPipe(pipe.sink());
        
        isClosed = false;
    }
    
    public OutputStream getOutput() {
        return out;
    }

    private int readBuffer() throws IOException {
        int i = readSel.selectedKeys().size();
        
        if(i == 0) { // in case avalable() was used as select.
            do {
                i = readSel.select(100);
            } while(!isClosed && i == 0);
        }
        
        if(i == 1) {
            Iterator it = readSel.selectedKeys().iterator();
            SelectionKey key = (SelectionKey) it.next();

            int read = 0;
            if(key.isReadable()) {
                read = source.read(readBuff);
            }

            it.remove();
            return read;
        }
        
        return -1;
    }
    
    private int fillBuffer() throws IOException {
        do {
            int read;
            if( (read = readBuffer()) == -1){
                if(readBuff.remaining() == readBuff.capacity()) {
                    return read;
                }
                
                break;
            }
        } while(readBuff.hasRemaining());
        
        return readBuff.capacity() - readBuff.remaining();
    }
    
    public int available() throws IOException {
        int s = readSel.selectNow();
        if(s == 0) {
            return -1;
        }
        
        return s;
    }
    
    public int read() throws IOException {
        throw new UnsupportedOperationException("Please use: public int read(byte b[], int off, int len) throws IOException");
    }
    
    public int read(byte b[], int off, int len) throws IOException {
        if(fillBuffer() == -1) {
            return -1;
        }
        
        readBuff.flip();

        int toGet = Math.min(len, readBuff.remaining());
    
        readBuff.get(b, off, toGet).compact();
        
        return toGet;
    }
    
    private class OutPipe extends OutputStream {
        private SinkChannel s;
        
        public OutPipe(SinkChannel sink) throws IOException {
            s = sink;
            s.configureBlocking(true);
        }
        
        public void write(int b) throws IOException {
            throw new UnsupportedOperationException("Please use: public void write(byte b[], int off, int len) throws IOException");
        }
        
        public void write(byte b[], int off, int len) throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
            s.write(buffer);
        }
        
        public void close() throws IOException {
            s.close();
            isClosed = true;
        }
    }
}
