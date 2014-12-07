/*
 * MessageReciever.java
 *
 * Created on October 31, 2005, 8:22 PM
 *
 */

package eunomia.core.receptor.listeners;

import eunomia.messages.Message;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface MessageReceiver {
    public void messageResponse(Message msg);
}
