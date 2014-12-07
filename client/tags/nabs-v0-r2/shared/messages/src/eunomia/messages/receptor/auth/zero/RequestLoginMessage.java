/*
 * RequestLoginMessage.java
 *
 * Created on February 21, 2006, 5:25 PM
 */

package eunomia.messages.receptor.auth.zero;

import eunomia.messages.receptor.auth.AuthenticationMessage;
import java.io.*;
/**
 *
 * @author Mikhail Sosonkin
 */
public class RequestLoginMessage implements AuthenticationMessage {
    private static final long serialVersionUID = 3813211758302744910L;
    
    private String login;

    public RequestLoginMessage() {
    }

    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }

    public void setLogin(String l){
        login = l;
    }
    
    public String getLogin(){
        return login;
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        login = in.readObject().toString();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(login);
    }
}