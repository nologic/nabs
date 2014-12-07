/*
 * ProtocolDescriptor.java
 *
 * Created on August 24, 2006, 11:48 PM
 *
 */

package eunomia.messages.receptor.protocol;

import eunomia.messages.Message;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ProtocolDescriptor extends Message {
    public String protoString();
}