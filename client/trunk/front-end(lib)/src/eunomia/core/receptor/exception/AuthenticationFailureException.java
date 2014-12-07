/*
 * AuthenticationFailureException.java
 *
 * Created on February 26, 2006, 12:18 AM
 */

package eunomia.core.receptor.exception;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AuthenticationFailureException extends Exception {
    public AuthenticationFailureException(String msg) {
        super(msg);
    }
}