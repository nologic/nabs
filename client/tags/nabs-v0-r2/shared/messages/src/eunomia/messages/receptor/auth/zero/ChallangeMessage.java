/*
 * ChallangeMessage.java
 *
 * Created on February 21, 2006, 5:38 PM
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
public class ChallangeMessage implements AuthenticationMessage {
    private static final long serialVersionUID = 3496425981282427507L;
    
    private byte[] challange;
    
    public ChallangeMessage() {
        challange = new byte[16];
    }
    
    public int getVersion() {
        return 0;
    }

    public void setVersion(int v) {
    }
    
    public byte[] getChallange() {
        return challange;
    }

    public void setChallange(byte[] challange) {
        this.challange = challange;
    }
    
    public void setChallange(byte[] rand, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec skey = new SecretKeySpec(key, "AES");
        
        cipher.init(Cipher.ENCRYPT_MODE, skey);
        cipher.doFinal(rand, 0, rand.length, challange);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readFully(challange);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.write(challange);
    }
}