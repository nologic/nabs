/*
 * NetworkChannelNewListener.java
 *
 * Created on March 23, 2008, 1:19 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.listeners;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NetworkChannelNewListener {
    public NetworkChannelActivityListener newChannel(NetworkChannel chan);
}