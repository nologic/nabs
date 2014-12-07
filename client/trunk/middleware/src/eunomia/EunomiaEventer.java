/*
 * NabAppender.java
 *
 * Created on November 27, 2006, 7:05 PM
 *
 */

package eunomia;

import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface EunomiaEventer {
    public void logEvent(LoggingEvent event);
}
