/*
 * RoleUpdateMessage.java
 *
 * Created on March 6, 2007, 10:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.com.atas;

import eunomia.messages.Message;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author kulesh
 */
public class RoleUpdateMessage implements Message{
    
    private long list[];
    private int length;
    private int roleNumber;
    
    /** Creates a new instance of RoleUpdateMessage */
    public RoleUpdateMessage() {
    }
    
    public RoleUpdateMessage(int roleNumber){
        setRoleNumber(roleNumber);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setRoleNumber(in.readInt());
        length= in.readInt();
        list= new long[length];
        
        for (int i = 0; i < length; ++i)
            list[i]= in.readLong();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(getRoleNumber());
        out.writeInt(length);
        for (int i = 0; i < length; ++i)
            out.writeLong(list[i]);
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public int getLength(){
        return length;
    }
    
    public long[] getList(){
        return list;
    }

    public void setList(long[] list, int n) {
        this.list = list;
        this.length= n;
    }

    public int getRoleNumber() {
        return roleNumber;
    }

    public void setRoleNumber(int roleNumber) {
        this.roleNumber = roleNumber;
    }
}
