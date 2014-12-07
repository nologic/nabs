/*
 * StateManager.java
 *
 * Created on July 31, 2006, 11:41 PM
 */

package eunomia.managers;

import eunomia.comm.UserState;
import eunomia.messages.receptor.ModuleHandle;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StateManager {
    public static StateManager ins;
    
    private HashMap userToState;
    private Set globals;
    
    public StateManager() {
        userToState = new HashMap();
        globals = new HashSet();
    }
    
    public UserState getState(String user){
        UserState state = (UserState)userToState.get(user);
        
        if(state == null){
            state = new UserState(user);
            Iterator it = globals.iterator();
            while (it.hasNext()) {
                ModuleHandle handle = (ModuleHandle) it.next();
                state.addProcHandle(handle);
            }
            
            userToState.put(user, state);
        }
        
        return state;
    }
    
    public void addGlobalInstance(ModuleHandle handle){
        globals.add(handle);
        
        if(userToState.size() > 0){
            Iterator it = userToState.values().iterator();
            while (it.hasNext()) {
                UserState state = (UserState) it.next();
                state.addProcHandle(handle);
            }
        }
    }
    
    public static StateManager v(){
        if(ins == null){
            ins = new StateManager();
        }

        return ins;
    }
}