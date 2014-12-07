/*
 * Reporter.java
 *
 * Created on April 23, 2008, 11:00 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface Reporter {
    public void commandAndControlChannel(NetworkChannel channel);
    public void executeSql(String sql);
}