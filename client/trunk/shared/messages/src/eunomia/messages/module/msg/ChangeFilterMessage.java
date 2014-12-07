/*
 * ChangeFilterMessage.java
 *
 * Created on January 7, 2006, 4:57 PM
 *
 */

package eunomia.messages.module.msg;

import eunomia.messages.FilterEntryMessage;
import eunomia.messages.module.AbstractModuleMessage;
import eunomia.messages.receptor.ModuleHandle;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChangeFilterMessage extends AbstractModuleMessage {
    private FilterEntryMessage[] wList;
    private FilterEntryMessage[] bList;
    
    private static final long serialVersionUID = 4537049044474476918L;
    
    public ChangeFilterMessage() {
    }

    public FilterEntryMessage[] getWhiteList() {
        return wList;
    }

    public void setWhiteList(FilterEntryMessage[] wList) {
        this.wList = wList;
    }

    public FilterEntryMessage[] getBlackList() {
        return bList;
    }

    public void setBlackList(FilterEntryMessage[] bList) {
        this.bList = bList;
    }
    
    public void resetForReturn(){
        wList = null;
        bList = null;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        if(wList != null){
            int count = wList.length;
            out.writeInt(count);
            for(int i = 0; i < count; i++){
                out.writeObject(wList[i]);
            }
        } else {
            out.writeInt(0);
        }

        if(bList != null){
            int count = bList.length;
            out.writeInt(count);
            for(int i = 0; i < count; i++){
                out.writeObject(bList[i]);
            }
        } else {
            out.writeInt(0);
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        int count = in.readInt();
        if(count != 0){
            wList = new FilterEntryMessage[count];
            for(int i = 0; i < count; i++){
                wList[i] = (FilterEntryMessage)in.readObject();
            }
        }
        
        count = in.readInt();
        if(count != 0){
            bList = new FilterEntryMessage[count];
            for(int i = 0; i < count; i++){
                bList[i] = (FilterEntryMessage)in.readObject();
            }
        }
    }
}