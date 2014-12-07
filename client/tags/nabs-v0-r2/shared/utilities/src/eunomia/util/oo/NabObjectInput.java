/*
 * NabObjectInput.java
 *
 * Created on March 5, 2006, 11:55 AM
 *
 */

package eunomia.util.oo;

import java.io.*;
import java.util.HashMap;
import eunomia.util.oo.listeners.NabObjectInputListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NabObjectInput extends InputStream implements ObjectInput {
    private static final int version = 1;
    private static final int magicnu = 0x44AABB55;
    private static final Class strClass = String.class;
    private static final HashMap classMap;

    private DataInputStream inStream;
    private DataInputStream in;
    private int streamVersion;
    private byte[] buffer;
    private byte[] classNameBuffer;
    private boolean readHeader;
    private int first;
    private int last;
    private int recursion;
    private LookUpString lookup;
    private boolean allowUnknowns;
    private static NabObjectInputListener listener;

    private static Logger logger;

    public NabObjectInput(InputStream i) {
        recursion = 0;
        allowUnknowns = false;
        lookup = new LookUpString();
        inStream = new DataInputStream(this);
        in = new DataInputStream(i);
        streamVersion = -1;
        readHeader = false;
        classNameBuffer = new byte[1024];
        buffer = new byte[4096];
        first = last = 0;
    }

    public static void setNabObjectInputListener(NabObjectInputListener l){
        listener = l;
    }

    public static void removeNabObjectInputListener(NabObjectInputListener l){
        if(listener == l){
            listener = null;
        }
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
                    throw new StreamCorruptedException("Unsupported version " + streamVersion + " required " + version + " or less");
                }

                if(inStream.readInt() == magicnu){
                    return;
                }
            }
        }

        throw new StreamCorruptedException("Invalid header");
    }

    private Externalizable lookupClass(Class klass) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        return (Externalizable)klass.newInstance();
    }

    private Class getClass(byte[] bytes, int len) throws IllegalAccessException, ClassNotFoundException {
        Class klass;

        int hash = 0;
        for (int i = 0; i < len; i++) {
            hash = 31*hash + bytes[i];
        }

        lookup.hash = hash;
        klass = (Class)classMap.get(lookup);
        if(klass == null/* && allowUnknowns*/){
            String cname = new String(bytes, 0, len);
            //logger.debug("Class not found: " + cname);
            return Class.forName(cname);
        }/* else if(!allowUnknowns){
            throw new IllegalAccessException("Unknown classses are not permitted: " + new String(bytes, 0, len));
        }*/

        return klass;
    }

    private int refillBuffer(int required) throws IOException {
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
    }

    public Object readObject() throws ClassNotFoundException, IOException {
        ++recursion;
        Object o = null;
        try {
            o = readObject_internal();
        } catch (IOException ex){
            throw ex;
        } catch (ClassNotFoundException ex) {
            throw ex;
        }

        if(--recursion == 0 && listener != null){
            listener.endReadingObject(this, o);
        }

        return o;
    }

    private Object readObject_internal() throws ClassNotFoundException, IOException {
        if(!readHeader){
            readHeader = true;
            readHeader();
        }

        if(inStream.readInt() == magicnu){
            if(recursion == 0 && listener != null){
                listener.beginReadingObject(this);
            }

            int clsLen = inStream.readInt();
            if(clsLen == 0) { // null pointer
                int version = inStream.readInt();
                if(inStream.readInt() != magicnu){
                    throw new StreamCorruptedException("Stream out of synch");
                }

                return null;
            } else if(clsLen < classNameBuffer.length){
                inStream.readFully(classNameBuffer, 0, clsLen);
                //String clsName = new String(classNameBuffer, 0, clsLen);
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
                        obj = lookupClass(klass);
                        if(obj instanceof Versioned){
                            Versioned vObj = (Versioned)obj;
                            version = in.readInt();
                            vObj.setVersion(version);
                        }

                        obj.readExternal(this);
                        if(inStream.readInt() == magicnu){
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

    static {
        logger = Logger.getLogger(NabObjectInput.class);
        classMap = new HashMap();
        try {
            classMap.put("java.lang.String", strClass);
            classMap.put("eunomia.messages.DatabaseDescriptor", Class.forName("eunomia.messages.DatabaseDescriptor"));
            classMap.put("eunomia.messages.FilterEntryMessage", Class.forName("eunomia.messages.FilterEntryMessage"));
            classMap.put("eunomia.messages.Message", Class.forName("eunomia.messages.Message"));
            classMap.put("eunomia.messages.module.AbstractModuleMessage", Class.forName("eunomia.messages.module.AbstractModuleMessage"));
            classMap.put("eunomia.messages.module.ModuleMessage", Class.forName("eunomia.messages.module.ModuleMessage"));
            classMap.put("eunomia.messages.module.msg.ActionMessage", Class.forName("eunomia.messages.module.msg.ActionMessage"));
            classMap.put("eunomia.messages.module.msg.ChangeFilterMessage", Class.forName("eunomia.messages.module.msg.ChangeFilterMessage"));
            classMap.put("eunomia.messages.module.msg.GenericModuleMessage", Class.forName("eunomia.messages.module.msg.GenericModuleMessage"));
            classMap.put("eunomia.messages.module.msg.GetFilterListMessage", Class.forName("eunomia.messages.module.msg.GetFilterListMessage"));
            classMap.put("eunomia.messages.module.msg.GetModuleControlDataMessage", Class.forName("eunomia.messages.module.msg.GetModuleControlDataMessage"));
            classMap.put("eunomia.messages.module.msg.InitialModuleStatusMessage", Class.forName("eunomia.messages.module.msg.InitialModuleStatusMessage"));
            classMap.put("eunomia.messages.module.msg.ModuleControlDataMessage", Class.forName("eunomia.messages.module.msg.ModuleControlDataMessage"));
            classMap.put("eunomia.messages.module.msg.ModuleStatusMessage", Class.forName("eunomia.messages.module.msg.ModuleStatusMessage"));
            classMap.put("eunomia.messages.receptor.AbstractCommandMessage", Class.forName("eunomia.messages.receptor.AbstractCommandMessage"));
            classMap.put("eunomia.messages.receptor.auth.zero.ChallangeMessage", Class.forName("eunomia.messages.receptor.auth.zero.ChallangeMessage"));
            classMap.put("eunomia.messages.receptor.auth.zero.ChallangeResponseMessage", Class.forName("eunomia.messages.receptor.auth.zero.ChallangeResponseMessage"));
            classMap.put("eunomia.messages.receptor.auth.zero.RequestLoginMessage", Class.forName("eunomia.messages.receptor.auth.zero.RequestLoginMessage"));
            classMap.put("eunomia.messages.receptor.auth.zero.ChallangeCheckStatusMessage", Class.forName("eunomia.messages.receptor.auth.zero.ChallangeCheckStatusMessage"));
            classMap.put("eunomia.messages.receptor.CommandMessage", Class.forName("eunomia.messages.receptor.CommandMessage"));
            classMap.put("eunomia.messages.receptor.ModuleHandle", Class.forName("eunomia.messages.receptor.ModuleHandle"));
            classMap.put("eunomia.messages.receptor.msg.cmd.AddDatabaseMessage", Class.forName("eunomia.messages.receptor.msg.cmd.AddDatabaseMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.AddStreamMessage", Class.forName("eunomia.messages.receptor.msg.cmd.AddStreamMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage", Class.forName("eunomia.messages.receptor.msg.cmd.CollectDatabaseMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage", Class.forName("eunomia.messages.receptor.msg.cmd.ConnectDatabaseMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage", Class.forName("eunomia.messages.receptor.msg.cmd.DatabaseQueryMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.GetModuleHandlesMessage", Class.forName("eunomia.messages.receptor.msg.cmd.GetModuleHandlesMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.InstantiateModuleMessage", Class.forName("eunomia.messages.receptor.msg.cmd.InstantiateModuleMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.RemoveDatabaseMessage", Class.forName("eunomia.messages.receptor.msg.cmd.RemoveDatabaseMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.RemoveStreamMessage", Class.forName("eunomia.messages.receptor.msg.cmd.RemoveStreamMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.SignalMessage", Class.forName("eunomia.messages.receptor.msg.cmd.SignalMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.StreamConnectionMessage", Class.forName("eunomia.messages.receptor.msg.cmd.StreamConnectionMessage"));
            classMap.put("eunomia.messages.receptor.msg.cmd.TerminateModuleMessage", Class.forName("eunomia.messages.receptor.msg.cmd.TerminateModuleMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.CommandResultMessage", Class.forName("eunomia.messages.receptor.msg.rsp.CommandResultMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.DatabaseTerminalOpenMessage", Class.forName("eunomia.messages.receptor.msg.rsp.DatabaseTerminalOpenMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.FailureMessage", Class.forName("eunomia.messages.receptor.msg.rsp.FailureMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.ModuleHandleListMessage", Class.forName("eunomia.messages.receptor.msg.rsp.ModuleHandleListMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.StatusMessage", Class.forName("eunomia.messages.receptor.msg.rsp.StatusMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.SuccessMessage", Class.forName("eunomia.messages.receptor.msg.rsp.SuccessMessage"));
            classMap.put("eunomia.messages.receptor.msg.rsp.UnknownMessage", Class.forName("eunomia.messages.receptor.msg.rsp.UnknownMessage"));
            classMap.put("eunomia.messages.receptor.ncm.ErrorMessage", Class.forName("eunomia.messages.receptor.ncm.ErrorMessage"));
            classMap.put("eunomia.messages.receptor.NoCauseMessage", Class.forName("eunomia.messages.receptor.NoCauseMessage"));
            classMap.put("eunomia.messages.receptor.ReceptorMessage", Class.forName("eunomia.messages.receptor.ReceptorMessage"));
            classMap.put("eunomia.messages.receptor.ResponceMessage", Class.forName("eunomia.messages.receptor.ResponceMessage"));
            classMap.put("eunomia.messages.receptor.ncm.ServerConnectionStatusMessage", Class.forName("eunomia.messages.receptor.ncm.ServerConnectionStatusMessage"));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class LookUpString {
        public int hash;

        public int hashCode(){
            return hash;
        }

        public boolean equals(Object o){
            return o.hashCode() == hash;
        }
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