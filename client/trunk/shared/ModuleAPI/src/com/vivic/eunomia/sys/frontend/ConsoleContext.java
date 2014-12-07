/*
 * ConsoleContext.java
 *
 * Created on December 25, 2007, 4:34 PM
 *
 */

package com.vivic.eunomia.sys.frontend;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ConsoleContext {
    private static ConsoleReceptor receptor;

    public static ConsoleReceptor getReceptor() {
        return receptor;
    }

    public static void setReceptor(ConsoleReceptor aReceptor) {
        if(receptor == null) {
            receptor = aReceptor;
        }
    }
}