/*
 * WontInstantiateException.java
 *
 * Created on September 11, 2008, 11:02 PM
 *
 */

package com.vivic.eunomia.sys.exception;

/**
 *
 * @author Mikhail Sosonkin
 */
public class WontInstantiateException extends RuntimeException {
    public WontInstantiateException(String msg) {
        super(msg);
    }
    
}