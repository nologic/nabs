/*
 * Descriptor.java
 *
 * Created on October 19, 2006, 10:52 PM
 *
 */

package com.vivic.eunomia.module;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface Descriptor {
    public static final int 
            TYPE_PROC = 0, // Flow processor.
            TYPE_FLOW = 1, // Flow producer.
            TYPE_ANLZ = 2, // Static analysis
            TYPE_COLL = 3, // Flow collection (to DB).
            TYPE_LIBB = 4; // 3rd party library.
    
    public String moduleName();
    public int moduleType();
    public String longDescription();
    public String shortDescription();
    public int version();
    public Dependency[] getDependencies();
}
