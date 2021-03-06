/*
 * DatabaseManagerListener.java
 *
 * Created on June 28, 2005, 7:12 PM
 *
 */

package eunomia.core.managers.listeners;

import eunomia.core.data.staticData.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DatabaseManagerListener {
    public void databaseAdded(Database db);
    public void databaseRemoved(Database db);
}
