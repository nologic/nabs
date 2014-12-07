/*
 * NabObjectInput.java
 *
 * Created on March 5, 2006, 11:55 AM
 *
 */

package eunomia.util.oo;

import eunomia.util.oo.exceptions.NotObjectException;
import java.io.*;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NabObjectInput extends InputStream implements ObjectInput {
    private static final int version = 1;
    private static final int magicnu = 0x44AABB55;
    private static final Class strClass = String.class;

    private DataInputStream inStream;
    private DataInputStream in;
    private int streamVersion;
    private byte[] buffer;
    private byte[] classNameBuffer;
    private boolean readHeader;
    /*private int first;
    private int last;*/
    private int recursion;
    private boolean allowUnknowns;
    private LargeTransferInputState ltState;
    private Object topLevelObject;
    private NabClassLocator cLoc;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(NabObjectInput.class);
    }
    
    public NabObjectInput(InputStream i, NabClassLocator ncl) {
        cLoc = ncl;
        recursion = 0;
        allowUnknowns = false;
        inStream = new DataInputStream(this);
        in = new DataInputStream(i);
        streamVersion = -1;
        readHeader = false;
        classNameBuffer = new byte[1024];
        buffer = new byte[4096];
        ltState = new LargeTransferInputState();
        //first = last = 0;
    }

    public boolean isAllowUnknowns() {
        return allowUnknowns;
    }

    public void setAllowUnknowns(boolean allowUnknowns) {
        this.allowUnknowns = allowUnknowns;
    }

    private void readHeader() throws IOException {
        if(inStream.readInt() == magicnu){
            if(inStream.readByte() == (byte)'V'){
                if( (streamVersion = inStream.readInt()) > version){
                    throw new StreamCorruptedException("Unsupported version " + streamVersion + ", required " + version + " or less");
                }

                if(inStream.readInt() == magicnu){
                    return;
                }
            }
        }

        throw new StreamCorruptedException("Invalid header");
    }

    private Externalizable getInstance(Class klass) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        return (Externalizable)klass.newInstance();
    }

    private Class getClass(byte[] bytes, int len) throws IllegalAccessException, ClassNotFoundException {
        int hash = 0;
        for (int i = 0; i < len; i++) {
            hash = 31*hash + bytes[i];
        }

        if(cLoc != null) {
            return cLoc.getClass(hash, bytes, len);
        }

        String cname = new String(bytes, 0, len);
        return Class.forName(cname);
    }

/*    private int refillBuffer(int required) throws IOException {
        int maxFree = 0;
        int available = in.available();
        int toFill = required;

        if(last > first){
            maxFree = 4096 - ((last - first) + 1);
        } else {
            maxFree = first - last - 1;
        }

        if(maxFree < available || maxFree < required){
            toFill = maxFree;
        } else if(maxFree > available){
            toFill = available;
        }

        if(last > first){
            //in.read(b
        } else {

        }

        return 0;
    }*/

    public Object readObject() throws ClassNotFoundException, IOException {
        ++recursion;
        
        boolean notObject = false;
        Object o = null;
        do {
            try {
                o = readObject_internal();
                notObject = false;
            } catch (NotObjectException ex) {
                notObject = true;
            }
        } while(notObject);
        
        --recursion;

        return o;
    }
    
    private Object processCommand() throws IOException, NotObjectException {
        int command = in.readInt();
        
        switch(command) {
            case NabObjectOutput.NULL_OBJECT: 
                return null;
            case NabObjectOutput.LARGE_TRANSFER: {
                LargeTransferState lt = ltState.findLargeTransfer(in.readInt());
                if(lt != null) {
                    lt.readPortion(this);
                    if(lt.isReadDone()) {
                        Object o = ltState.removeLargeTransfer(lt);
                        if(o != null){
                            return o;
                        }
                    }
                }
                
                throw new NotObjectException("");
            }
        }
        
        throw new StreamCorruptedException("Unknown command: " + command);
    }

    private Object readObject_internal() throws ClassNotFoundException, IOException, NotObjectException {
        if(!readHeader){
            readHeader = true;
            readHeader();
        }

        if(inStream.readInt() == magicnu){
            int clsLen = inStream.readInt();
            if(clsLen == 0) { // command
                return processCommand();
            } else if(clsLen < classNameBuffer.length){
                inStream.readFully(classNameBuffer, 0, clsLen);
                Class klass = null;
                try {
                    klass = getClass(classNameBuffer, clsLen);
                } catch (IllegalAccessException iae){
                    throw new StreamCorruptedException(iae.getMessage());
                }

                Externalizable obj = null;
                if(klass == strClass){
                    int version = inStream.readInt();
                    int strLen = inStream.readInt();
                    byte[] strB = new byte[strLen];
                    inStream.readFully(strB);
                    if(inStream.readInt() != magicnu){
                        throw new StreamCorruptedException("Stream out of synch");
                    }
                    return new String(strB);
                } else {
                    try {
                        int version;
                        obj = getInstance(klass);
                        
                        if(recursion == 1){
                            topLevelObject = obj;
                        }
                        
                        if(obj instanceof Versioned){
                            Versioned vObj = (Versioned)obj;
                            version = in.readInt();
                            vObj.setVersion(version);
                        } 

                        obj.readExternal(this);
                        if(inStream.readInt() == magicnu){
                            if(obj instanceof LargeTransfer) {
                                ltState.initiateLargeTransfer((LargeTransfer)obj, topLevelObject);
                            } /*else if(obj == topLevelObject && !ltState.isAvailable(topLevelObject)) {
                                throw new NotObjectException("");
                            }*/
                            
                            return obj;
                        }
                    } catch (IllegalAccessException iae){
                        throw new StreamCorruptedException(iae.getMessage());
                    } catch (ClassNotFoundException cnfe){
                        throw new StreamCorruptedException(cnfe.getMessage());
                    } catch (InstantiationException ie){
                        throw new StreamCorruptedException(ie.getMessage());
                    }
                }
            }
        }
        
        throw new StreamCorruptedException("Stream out of synch");
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException("No skipping");
    }

    public int available() throws IOException {
        return in.available();
    }

    public void close() throws IOException {
        in.close();
    }

    public void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte b[], int off, int len) throws IOException {
        inStream.readFully(b, off, len);
    }

    public int skipBytes(int n) throws IOException{
        return inStream.skipBytes(n);
    }

    public boolean readBoolean() throws IOException{
        return inStream.readBoolean();
    }

    public byte readByte() throws IOException{
        return inStream.readByte();
    }

    public int readUnsignedByte() throws IOException{
        return inStream.readUnsignedByte();
    }

    public short readShort() throws IOException{
        return inStream.readShort();
    }

    public int readUnsignedShort() throws IOException{
        return inStream.readUnsignedShort();
    }

    public char readChar() throws IOException{
        return inStream.readChar();
    }

    public int readInt() throws IOException{
        return inStream.readInt();
    }

    public long readLong() throws IOException{
        return inStream.readLong();
    }

    public float readFloat() throws IOException {
        return inStream.readFloat();
    }

    public double readDouble() throws IOException{
        return inStream.readDouble();
    }

    public String readLine() throws IOException{
        return inStream.readLine();
    }

    public String readUTF() throws IOException {
        return inStream.readUTF();
    }
}