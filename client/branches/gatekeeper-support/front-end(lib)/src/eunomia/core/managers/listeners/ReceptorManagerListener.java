/*
 * ReceptorManagerListener.java
 *
 * Created on December 1, 2005, 9:01 PM
 *
 */

package eunomia.core.managers.listeners;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorManagerListener {
    public void receptorAdded(Receptor rec);
    public void receptorRemoved(Receptor rec);
}