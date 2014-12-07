/*
 * NabObjectOutput.java
 *
 * Created on November 28, 2006, 7:32 PM (while over the pacific)
 *
 */

package eunomia.util.oo.listeners;

import eunomia.util.oo.NabObjectOutput;

/**
 *
 * NOT thread safe
 *
 * @author Mikhail Sosonkin
 */
public interface NabObjectOutputListener {
    /*
     * Only the top level object.
     */
    public void beginWrittingObject(NabObjectOutput out, Object o);
    public void endWrittingObject(NabObjectOutput out, Object o);
}