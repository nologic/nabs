/*
 * ErrorMessanger.java
 *
 * Created on April 16, 2006, 12:13 PM
 */

package eunomia.comm.interfaces;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ErrorMessenger {
    public void error(Object source, String msg);
}
