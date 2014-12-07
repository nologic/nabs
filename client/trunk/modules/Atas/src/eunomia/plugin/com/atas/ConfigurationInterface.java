/*
 * ConfigurationInterface.java
 *
 * Created on April 11, 2007, 8:07 PM
 *
 */

package eunomia.plugin.com.atas;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ConfigurationInterface {
    public void setConfigurationMessage(ClassifierConfigurationMessage msg);
    public ClassifierConfigurationMessage getConfigurationMessage();
}
