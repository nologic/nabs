/*
 * NabClassLocator.java
 *
 * Created on August 5, 2007, 3:57 PM
 *
 */

package eunomia.util.oo;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NabClassLocator {
    public Class getClass(int hash, byte[] bytes, int len) throws IllegalAccessException, ClassNotFoundException;
}
