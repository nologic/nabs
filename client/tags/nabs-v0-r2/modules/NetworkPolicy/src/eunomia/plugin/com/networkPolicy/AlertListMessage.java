/*
 * AlertListMessage.java
 *
 * Created on December 17, 2006, 3:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.com.networkPolicy;

import eunomia.messages.Message;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author kulesh
 */
public class AlertListMessage implements Message{
    
    private long[] list;
    private int length;
    
    /** Creates a new instance of AlertListMessage */
    public AlertListMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(length);
        for(int i=0; i < length; ++i)
            out.writeLong(list[i]);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        list= new long[in.readInt()];
        for(int i=0; i < list.length; ++i)
            list[i]= in.readLong();
        length= list.length;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public long[] getList() {
        return list;
    }

    public void setList(long[] list, int length) {
        this.list = list;
        this.length= length;
    }
    
    public int getLength(){
        return length;
    }

}
