/*
 * NabObjectOutput.java
 *
 * Created on March 4, 2006, 9:23 AM
 *
 */

package eunomia.util.oo;

import java.io.*;
import eunomia.util.oo.listeners.NabObjectOutputListener;

/**
 *
 * NOT thread safe
 *
 * @author Mikhail Sosonkin
 */
public class NabObjectOutput extends OutputStream implements ObjectOutput {
    private static final int version = 1;
    private static final int magicnu = 0x44AABB55;
    private static final int max_buf = 128 * 1024; // 128KB
    private static final Class strClass = String.class;

    private DataOutputStream out;
    private DataOutputStream outStream;
    private byte[] buffer;
    private int count;
    private int recursion;
    private boolean wroteHeader;
    private static NabObjectOutputListener listener; // should this be static, no if it's needed for something else

    public NabObjectOutput(OutputStream os) {
        outStream = new DataOutputStream(os);
        out = new DataOutputStream(this);
        wroteHeader = false;
        buffer = new byte[max_buf];
    }

    public static void setNabObjectOutputListener(NabObjectOutputListener l){
        listener = l;
    }

    public static void removeNabObjectOutputListener(NabObjectOutputListener l){
        if(listener == l){
            listener = null;
        }
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
     * <Magic> <classname-length> <classname> <Obj-version if Versioned | String | null obj> <Magic> <bytes>
     *
     */
    public void writeObject(Object obj) throws IOException {
        if(recursion == 0 && listener != null){
            listener.beginWrittingObject(this, obj);
        }

        if(!wroteHeader){
            wroteHeader = true;
            writeHeader();
        }

        recursion++;
        if(obj == null){
            out.writeInt(magicnu);
            out.writeInt(0);
            out.writeInt(0); // verison;
            out.writeInt(magicnu);
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
        } else {
            throw new StreamCorruptedException("Only String or Externalizable can be written.\n\t" + obj.getClass().getName());
        }
        recursion--;

        if(recursion == 0){
            outStream.write(buffer, 0, count);
            count = 0;
            flush();
            if(listener != null) {
                listener.endWrittingObject(this, obj);
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

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
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
            throw new IOException("Buffer is full. Max size: " + max_buf + ". Use alternate communication.");
        }
        System.arraycopy(b, off, buffer, count, len);
        count = newcount;
    }

    public void flush() throws IOException {
        outStream.flush();
    }

    public void close() throws IOException {
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