/*
 * ReceptorListener.java
 *
 * Created on February 26, 2006, 12:38 AM
 */

package eunomia.core.receptor.listeners;

import eunomia.core.receptor.Receptor;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReceptorListener {
    public void receptorDisconnected(Receptor rec);
    public void receptorConnected(Receptor rec);
}