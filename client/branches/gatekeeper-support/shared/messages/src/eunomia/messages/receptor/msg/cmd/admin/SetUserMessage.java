/*
 * AddUserMessage.java
 *
 * Created on September 28, 2006, 11:21 PM
 *
 */

package eunomia.messages.receptor.msg.cmd.admin;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SetUserMessage extends AdminMessage {
    private String user;
    private String pass;
    private String old_pass;
    
    public SetUserMessage() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(user);
        out.writeObject(pass);
        out.writeObject(old_pass);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        user = (String)in.readObject();
        pass = (String)in.readObject();
        old_pass = (String)in.readObject();
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public String getOldPass() {
        return old_pass;
    }

    public void setOldPass(String old_pass) {
        this.old_pass = old_pass;
    }
    
}