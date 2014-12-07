/*
 * ResponceMessage.java
 *
 * Created on September 6, 2005, 2:50 PM
 */

package eunomia.messages.receptor;

import eunomia.messages.Message;


/**
 *
 * @author Mikhail Sosonkin
 */
public interface ResponceMessage extends ReceptorMessage {
    public Message getCause();
}