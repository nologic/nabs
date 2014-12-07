/*
 * NabObjectInput.java
 *
 * Created on November 28, 2006, 7:32 PM (while over the pacific)
 *
 */

package eunomia.util.oo.listeners;

import eunomia.util.oo.NabObjectInput;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NabObjectInputListener {
    /*
     * Only the top level object.
     */
    public void beginReadingObject(NabObjectInput out);
    public void endReadingObject(NabObjectInput out, Object o);
}