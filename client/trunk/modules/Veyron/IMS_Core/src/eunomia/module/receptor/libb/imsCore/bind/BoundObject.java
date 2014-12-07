/*
 * BoundClass.java
 *
 * Created on January 27, 2008, 10:52 AM
 *
 */

package eunomia.module.receptor.libb.imsCore.bind;

import java.io.Serializable;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface BoundObject extends Serializable {
    public int getByteSize();
    public void serialize(byte[] arr, int offset);
    public void unserialize(byte[] arr, int offset);
    public BoundObject clone();
}