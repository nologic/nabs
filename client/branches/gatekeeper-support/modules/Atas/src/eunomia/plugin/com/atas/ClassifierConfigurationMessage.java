/*
 * ClassifierConfigurationMessage.java
 *
 * Created on April 11, 2007, 7:21 PM
 *
 */

package eunomia.plugin.com.atas;

import eunomia.messages.Message;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ClassifierConfigurationMessage extends Message {
    public int getRoleNumber();
    public String getRoleName();
    public String getClassName();
}