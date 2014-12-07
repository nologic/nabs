/*
 * LargeTransferInputState.java
 *
 * Created on February 14, 2007, 9:44 PM
 *
 */

package eunomia.util.oo;

import eunomia.util.number.ModInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Mikhail Sosonkin
 */
class LargeTransferInputState {
    private ConcurrentHashMap idToTrans;
    private Set waitingMessages;
    private ModInteger modInt;
    
    LargeTransferInputState() {
        modInt = new ModInteger();
        waitingMessages = new HashSet();
        idToTrans = new ConcurrentHashMap();
    }
    
    void initiateLargeTransfer(LargeTransfer lt, Object o) {
        modInt.setInt(lt.getIdentifier());
        idToTrans.put(modInt, new LargeTransferState(lt, o));
        waitingMessages.add(o);
    }
    
    LargeTransferState findLargeTransfer(int id){
        modInt.setInt(id);
        return (LargeTransferState)idToTrans.get(modInt);
    }
    
    boolean isObjectComplete(Object o){
        Iterator it = idToTrans.values().iterator();
        while (it.hasNext()) {
            LargeTransferState lts = (LargeTransferState) it.next();
            if(lts.getAssiciation() == o) {
                return false;
            }
        }
        
        return true;
    }
    
    Object removeLargeTransfer(LargeTransferState state) {
        modInt.setInt(state.getTransfer().getIdentifier());
        idToTrans.remove(modInt);
        
        Object assc = state.getAssiciation();
        if(isObjectComplete(assc)) {
            waitingMessages.remove(assc);
            return assc; // Message can now be dispatched.
        }
        
        return null;
    }    

    boolean isAvailable(Object o) {
        return !waitingMessages.contains(o);
    }
}
