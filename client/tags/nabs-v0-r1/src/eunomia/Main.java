/*
 * Main.java
 *
 * Created on May 31, 2005, 4:40 PM
 */

package eunomia;

import eunomia.gui.MainGui;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class Main {
    public static void main(String[] argv) {
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        
        MainGui.main(argv);
    }
}