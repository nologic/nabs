/*
 * ClientConnectMessage.java
 *
 * Created on September 8, 2005, 5:57 PM
 *
 */

package eunomia.messages.receptor.msg.rsp;

import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.*;
import eunomia.messages.receptor.protocol.ProtocolDescriptor;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StatusMessage extends AbstractCommandMessage {
    private StreamServer[] servs;
    private ModuleDescriptor[] modules;
    private String[] jdbcList;
    private DatabaseDescriptor[] databases;
    
    private static final long serialVersionUID = 5219307962664702116L;
    
    public StatusMessage() {
        //modNames = new String[]{};
        databases = new DatabaseDescriptor[]{};
    }
    
    public void setDatabaseCount(int count){
        if(readOnly){
            throw new UnsupportedOperationException("setDatabaseCount: StatusMessage is read only");
        }
        
        databases = new DatabaseDescriptor[count];
    }
    
    public void setDatabase(int i, DatabaseDescriptor db){
        if(readOnly){
            throw new UnsupportedOperationException("setDatabase: StatusMessage is read only");
        }
        
        databases[i] = db;
    }
    
    public void setServersCount(int count){
        if(readOnly){
            throw new UnsupportedOperationException("setServersCount: StatusMessage is read only");
        }
        
        servs = new StreamServer[count];
        for(int i = count - 1; i != -1; --i){
            servs[i] = new StreamServer();
        }
    }
    
    public void setServer(int i, ProtocolDescriptor protocol, String name, String mod, boolean isConnected){
        if(readOnly){
            throw new UnsupportedOperationException("setServer: StatusMessage is read only");
        }
                
        servs[i].setName(name);
        servs[i].setModUsed(mod);
        servs[i].setConnected(isConnected);
        servs[i].setProtocol(protocol);
    }
    
    public StreamServer getServer(int i){
        return servs[i];
    }
    
    public int getServerCount(){
        return servs.length;
    }
    
    public void setModuleCount(int count) {
        if(readOnly){
            throw new UnsupportedOperationException("setModuleCount: StatusMessage is read only");
        }
        
        modules = new ModuleDescriptor[count];
        for (int i = 0; i < modules.length; i++) {
            modules[i] = new ModuleDescriptor();
        }
    }
    
    public ModuleDescriptor[] getModules() {
        return modules;
    }
    
    public void setModule(int i, String name, int type, byte[] hash) {
        if(readOnly){
            throw new UnsupportedOperationException("setModule: StatusMessage is read only");
        }
        
        modules[i].setName(name);
        modules[i].setType(type);
        modules[i].setHash(hash);
    }
    
    public DatabaseDescriptor[] getDatabases(){
        return databases;
    }
    
    public int getCommandID() {
        return CommandMessage.CMD_STATUS;
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(servs.length);
        for(int i = 0; i < servs.length; i++){
            out.writeObject(servs[i]);
        }
        
        out.writeInt(databases.length);
        for(int i = 0; i < databases.length; i++){
            out.writeObject(databases[i]);
        }
        
        out.writeInt(jdbcList.length);
        for (int i = 0; i < jdbcList.length; i++) {
            out.writeObject(jdbcList[i]);
        }
        
        out.writeInt(modules.length);
        for (int i = 0; i < modules.length; i++) {
            out.writeObject(modules[i]);
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        
        int count = in.readInt();
        servs = new StreamServer[count];
        for(int i = 0; i < count; i++){
            servs[i] = (StreamServer)in.readObject();
        }
        
        count = in.readInt();
        databases = new DatabaseDescriptor[count];
        for(int i = 0; i < count; i++){
            databases[i] = (DatabaseDescriptor)in.readObject();
        }
        
        count = in.readInt();
        jdbcList = new String[count];
        for(int i = 0; i < count; i++){
            jdbcList[i] = (String)in.readObject();
        }
        
        count = in.readInt();
        modules = new ModuleDescriptor[count];
        for (int i = 0; i < modules.length; i++) {
            modules[i] = (ModuleDescriptor)in.readObject();
        }
    }
    
    public String[] getJdbcList() {
        return jdbcList;
    }

    public void setJdbcList(String[] jdbcList) {
        this.jdbcList = jdbcList;
    }
    
    public static class StreamServer implements Externalizable {
        private String name;
        private String modUsed;
        private boolean isConnected;
        private ProtocolDescriptor desc;
        
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
            out.writeObject(modUsed);
            out.writeObject(desc);
            out.writeBoolean(isConnected);
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            name = in.readObject().toString();
            modUsed = in.readObject().toString();
            desc = (ProtocolDescriptor)in.readObject();
            isConnected = in.readBoolean();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public boolean isConnected() {
            return isConnected;
        }

        public void setConnected(boolean isConnected) {
            this.isConnected = isConnected;
        }

        public String getModUsed() {
            return modUsed;
        }

        public void setModUsed(String modUsed) {
            this.modUsed = modUsed;
        }

        public ProtocolDescriptor getProtocol() {
            return desc;
        }

        public void setProtocol(ProtocolDescriptor protocol) {
            this.desc = protocol;
        }
    }
    
    public static class ModuleDescriptor implements Externalizable {
        private String name;
        private byte[] hash;
        private int type;
        
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
            out.write(hash); // (MD5) 16 byte assumption
            out.writeInt(type);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            name = (String)in.readObject();
            hash = new byte[16];
            in.read(hash);
            type = in.readInt();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getHash() {
            return hash;
        }

        public void setHash(byte[] hash) {
            if(hash.length != 16) {
                throw new RuntimeException("Hash Length MUST be 16 bytes long");
            }
            
            this.hash = hash;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}