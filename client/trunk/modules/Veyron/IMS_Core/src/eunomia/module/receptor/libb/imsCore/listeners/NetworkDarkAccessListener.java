/*
 * NetworkDarkAccessListener.java
 *
 * Created on September 11, 2008, 8:27 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.listeners;

import eunomia.module.receptor.libb.imsCore.net.DarkAccess;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NetworkDarkAccessListener {
    public void newDarkAccess(DarkAccess da);
}