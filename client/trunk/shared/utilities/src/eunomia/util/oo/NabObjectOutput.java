/*
 * NabObjectOutput.java
 *
 * Created on March 4, 2006, 9:23 AM
 *
 */

package eunomia.util.oo;

import java.io.*;

/**
 *
 * NOT thread safe
 *
 * @author Mikhail Sosonkin
 */
public class NabObjectOutput extends OutputStream implements ObjectOutput {
    public static final int NULL_OBJECT = 0;
    public static final int LARGE_TRANSFER = 1;
    
    public static final int max_buf = 256 * 1024; // 256KB
    private static final int version = 1;
    private static final int magicnu = 0x44AABB55;
    private static final Class strClass = String.class;

    private DataOutputStream out;
    private DataOutputStream outStream;
    private byte[] buffer;
    private int count;
    private int recursion;
    private boolean wroteHeader;
    
    private LargeTransferSendThread ltSend;

    public NabObjectOutput(OutputStream os) {
        outStream = new DataOutputStream(os);
        out = new DataOutputStream(this);
        
        wroteHeader = false;
        buffer = new byte[max_buf];
        ltSend = new LargeTransferSendThread(this);
    }

    /*
     *
     * Header:
     * <Magic> V <version> <Magic>
     *
     */

    private void writeHeader() throws IOException {
        outStream.writeInt(magicnu);
        outStream.writeByte((byte)'V');
        outStream.writeInt(version);
        outStream.writeInt(magicnu);
    }

    /*
     * Class format:
     * <Magic> <classname-length || 0> <classname> <Obj-version if Versioned || String || null obj> <Magic> <bytes>
     *
     */
    public void writeObject(Object obj) throws IOException {
        synchronized(this) {
            if(!wroteHeader){
                wroteHeader = true;
                writeHeader();
            }

            recursion++;
            if(obj == null){
                out.writeInt(magicnu);
                out.writeInt(0); // class size
                out.writeInt(NULL_OBJECT); // command
            } else if(obj instanceof LargeTransferState) {
                LargeTransferState ltg = (LargeTransferState)obj;
                out.writeInt(magicnu);
                out.writeInt(0); // class size
                out.writeInt(LARGE_TRANSFER); // command
                out.writeInt(ltg.getTransfer().getIdentifier());
                ltg.writePortion(this); // this will fill the buffer.
            } else if(obj instanceof String){
                String classname = strClass.getName();
                out.writeInt(magicnu);
                out.writeInt(classname.length());
                out.write(classname.getBytes());
                out.writeInt(0); // String version;

                String objString = (String)obj;
                out.writeInt(objString.length());
                out.write(objString.getBytes());
                //close object
                out.writeInt(magicnu);
            } else if(obj instanceof Externalizable){
                String classname = obj.getClass().getName();
                out.writeInt(magicnu);
                out.writeInt(classname.length());
                out.write(classname.getBytes());
                if(obj instanceof Versioned){
                    out.writeInt(((Versioned)obj).getVersion());
                }

                ((Externalizable)obj).writeExternal(this);

                //close object
                out.writeInt(magicnu);
                if(obj instanceof LargeTransfer) {
                    LargeTransfer lt = (LargeTransfer)obj;
                    ltSend.addLargeTransfer(lt);
                } 
            } else {
                throw new StreamCorruptedException("Not compatible object.\n\t" + obj.getClass().getName());
            }

            recursion--;

            if(recursion == 0){
                outStream.write(buffer, 0, count);
                count = 0;
                flush();
            }
        }
    }

    public void write(int b) throws IOException {
    	int newcount = count + 1;
    	if (newcount > buffer.length) {
                throw new IOException("Buffer is full. Max size: " + max_buf + " Bytes. Use alternate communication.");
    	}
    	buffer[count] = (byte)b;
    	count = newcount;
    }
    
    public int available() {
        return buffer.length - count;
    }
    
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public int write(InputStream in, int avail) throws IOException {
        return write(in, avail, false);
    }

    public int write(InputStream in, int size, boolean sizePrefix) throws IOException {
        int savedCount = count;
        int read;
        int totalRead = 0;
        
        if(sizePrefix) {
            count += 4; // reserve space for size.
        }
        
        if(size > available()) {
            size = available();
        }
        
        while(available() > 0 && (read = in.read(buffer, count, size - totalRead)) != -1) {
            count += read;
            totalRead += read;
        }
        
        if(sizePrefix) {
            int newCount = count;
            count = savedCount;

            writeInt(totalRead);
            count = newCount;
        }
        return totalRead;
    }
    
    public void write(byte b[], int off, int len) throws IOException {
    	if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
    	    throw new IndexOutOfBoundsException();
    	} else if (len == 0) {
    	    return;
    	}

        int newcount = count + len;
        if (newcount > buffer.length) {
            count = 0;
            throw new IOException("Buffer is full. Max size: " + max_buf + ". Use alternate communication.");
        }
        System.arraycopy(b, off, buffer, count, len);
        count = newcount;
    }

    public void flush() throws IOException {
        outStream.flush();
    }

    public void close() throws IOException {
        ltSend.close();
        outStream.close();
    }

    public void writeBoolean(boolean v) throws IOException {
        out.writeBoolean(v);
    }

    public void writeByte(int v) throws IOException {
        out.writeByte(v);
    }

    public void writeShort(int v) throws IOException {
        out.writeShort(v);
    }

    public void writeChar(int v) throws IOException {
        out.writeChar(v);
    }

    public void writeInt(int v) throws IOException {
        out.writeInt(v);
    }

    public void writeLong(long v) throws IOException {
        out.writeLong(v);
    }

    public void writeFloat(float v) throws IOException {
        out.writeFloat(v);
    }

    public void writeDouble(double v) throws IOException {
        out.writeDouble(v);
    }

    public void writeBytes(String s) throws IOException {
        out.writeBytes(s);
    }

    public void writeChars(String s) throws IOException {
        out.writeChars(s);
    }

    public void writeUTF(String str) throws IOException {
        out.writeUTF(str);
    }
}