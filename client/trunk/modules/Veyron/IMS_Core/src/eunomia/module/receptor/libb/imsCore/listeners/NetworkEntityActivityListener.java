/*
 * NetworkEntityActivityListner.java
 *
 * Created on March 16, 2008, 9:52 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.listeners;

import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;
import eunomia.module.receptor.libb.imsCore.net.NetworkEntityHostKey;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NetworkEntityActivityListener {
    public boolean isStillInterested(NetworkEntityHostKey host, long lastNotify);
    public boolean entityActivity(NetworkEntity ent);
}