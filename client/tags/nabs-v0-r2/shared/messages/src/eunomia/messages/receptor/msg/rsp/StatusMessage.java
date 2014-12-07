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
    private String[] modNames;
    private String[] flowModNames;
    private String[] analModNames;
    private String[] jdbcList;
    private DatabaseDescriptor[] databases;
    
    private static final long serialVersionUID = 5219307962664702116L;
    
    public StatusMessage() {
        modNames = new String[]{};
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
    
    public void setModuleNames(String[] names){
        if(readOnly){
            throw new UnsupportedOperationException("setModuleNames: StatusMessage is read only");
        }
        
        modNames = names;
    }
    
    public String[] getModuleNames(){
        return modNames;
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
    
    public String[] getFlowModuleNames() {
        return flowModNames;
    }

    public void setFlowModuleNames(String[] flowModNames) {
        if(readOnly){
            throw new UnsupportedOperationException("setFlowModuleNames: StatusMessage is read only");
        }
                
        this.flowModNames = flowModNames;
    }
    
    public String[] getAnalysisModuleNames() {
        return analModNames;
    }
    
    public void setAnalysisModuleNames(String[] names){
        if(readOnly){
            throw new UnsupportedOperationException("setAnalysisModuleNames: StatusMessage is read only");
        }

        analModNames = names;
    }
    
    public String[] getJdbcList() {
        return jdbcList;
    }

    public void setJdbcList(String[] jdbcList) {
        this.jdbcList = jdbcList;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeInt(servs.length);
        for(int i = 0; i < servs.length; i++){
            out.writeObject(servs[i]);
        }
        
        out.writeInt(modNames.length);
        for(int i = 0; i < modNames.length; i++){
            out.writeObject(modNames[i]);
        }
        
        out.writeInt(databases.length);
        for(int i = 0; i < databases.length; i++){
            out.writeObject(databases[i]);
        }
        
        out.writeInt(flowModNames.length);
        for(int i = 0; i < flowModNames.length; i++){
            out.writeObject(flowModNames[i]);
        }
        
        out.writeInt(analModNames.length);
        for(int i = 0; i < analModNames.length; i++){
            out.writeObject(analModNames[i]);
        }
        
        out.writeInt(jdbcList.length);
        for (int i = 0; i < jdbcList.length; i++) {
            out.writeObject(jdbcList[i]);
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
        modNames = new String[count];
        for(int i = 0; i < count; i++){
            modNames[i] = (String)in.readObject();
        }
        
        count = in.readInt();
        databases = new DatabaseDescriptor[count];
        for(int i = 0; i < count; i++){
            databases[i] = (DatabaseDescriptor)in.readObject();
        }
        
        count = in.readInt();
        flowModNames = new String[count];
        for(int i = 0; i < count; i++){
            flowModNames[i] = (String)in.readObject();
        }

        count = in.readInt();
        analModNames = new String[count];
        for(int i = 0; i < count; i++){
            analModNames[i] = (String)in.readObject();
        }
        
        count = in.readInt();
        jdbcList = new String[count];
        for(int i = 0; i < count; i++){
            jdbcList[i] = (String)in.readObject();
        }
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
}
