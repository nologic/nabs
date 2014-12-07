/*
 * NetworkEntityNewListener.java
 *
 * Created on March 23, 2008, 1:20 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.listeners;

import eunomia.module.receptor.libb.imsCore.net.NetworkEntity;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NetworkEntityNewListener {
    public NetworkEntityActivityListener newEntity(NetworkEntity ent);
}