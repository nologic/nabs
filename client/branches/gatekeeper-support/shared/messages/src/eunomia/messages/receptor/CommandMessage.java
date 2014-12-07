/*
 * CommandMessage.java
 *
 * Created on September 6, 2005, 2:50 PM
 *
 */

package eunomia.messages.receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface CommandMessage extends ReceptorMessage {
    public static final int CMD_STATUS = -1;
    
    public static final int CMD_ADD_SERVER = 0;
    public static final int CMD_REMOVE_SERVER = 1;
    
    public static final int CMD_ADD_DATABASE = 2;
    public static final int CMD_REMOVE_DATABASE = 3;
    
    public static final int CMD_CONNECT_SERVER = 4;
    public static final int CMD_DISCONNECT_SERVER = 5;
    
    public static final int CMD_COLLECT_DB = 6;
    public static final int CMD_NOTCOLLECT_DB = 7;

    public int getCommandID();
}