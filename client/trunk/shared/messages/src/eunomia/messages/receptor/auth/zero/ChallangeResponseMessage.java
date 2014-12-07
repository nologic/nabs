/*
 * ChallangeResponseMessage.java
 *
 * Created on February 25, 2006, 5:30 PM
 *
 */

package eunomia.messages.receptor.auth.zero;

import eunomia.messages.receptor.auth.AuthenticationMessage;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ChallangeResponseMessage implements AuthenticationMessage {
    private static final long serialVersionUID = 8912032201281009725L;
    private byte[] response;
    
    public ChallangeResponseMessage() {
        response = new byte[16];
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public void produceResponse(byte[] enc, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec skey = new SecretKeySpec(key, "AES");
        
        cipher.init(Cipher.DECRYPT_MODE, skey);
        cipher.doFinal(enc, 0, enc.length, response);
    }
    
    public byte[] getResponse(){
        return response;
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readFully(response);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(response);
    }
}
