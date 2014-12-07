/*
 * PolicyListMessage.java
 *
 * Created on December 17, 2006, 3:39 PM
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
public class PolicyListMessage implements Message{
    
    private int[] list;
    private int length;
    
    /** Creates a new instance of PolicyListMessage */
    public PolicyListMessage() {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(length);
        for(int i = 0; i < length; ++i) {
            out.writeInt(list[i]);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        list = new int[in.readInt()];
        for(int i = 0; i < list.length; ++i) {
            list[i] = in.readInt();
        }
        
        length = list.length;
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public int[] getList() {
        return list;
    }

    public void setList(int[] list, int length) {
        this.list = list;
        this.length = length;
    }
    
    public int getLength(){
        return length;
    }
    
}
