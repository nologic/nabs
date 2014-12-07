/*
 * MessageSender.java
 *
 * Created on May 21, 2007, 9:02 PM
 *
 */

package eunomia.plugin.gui.lossyHistogram;

import java.io.IOException;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface MessageSender {
    public void sendObject(Object o) throws IOException;
}
