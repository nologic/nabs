/*
 * ModuleMessage.java
 *
 * Created on September 6, 2005, 2:48 PM
 *
 */

package eunomia.messages.module;

import eunomia.messages.Message;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ModuleMessage extends Message {
    public int getModuleID();
}
