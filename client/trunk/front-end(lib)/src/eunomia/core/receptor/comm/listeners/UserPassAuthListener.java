/*
 * UserPassAuthListener.java
 *
 * Created on October 12, 2006, 10:20 PM
 *
 */

package eunomia.core.receptor.comm.listeners;

import eunomia.core.receptor.comm.UserPassAuth;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface UserPassAuthListener {
    public void authenticationFailed(UserPassAuth a);
    public void authenticationSucceeded(UserPassAuth a);
}
