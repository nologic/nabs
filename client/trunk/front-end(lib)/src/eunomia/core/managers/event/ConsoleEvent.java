/*
 * ManagerEvent.java
 *
 * Created on May 15, 2007, 9:43 PM
 *
 */

package eunomia.core.managers.event;

import java.util.EventObject;

/**
 *
 * @author Mikhail Sosonkin
 */
public abstract class ConsoleEvent extends EventObject {
    public ConsoleEvent(Object source) {
        super(source);
    }
}