/*
 * Main.java
 *
 * Created on May 31, 2005, 4:40 PM
 */

package eunomia;

import eunomia.gui.MainGui;
import java.io.File;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class Main {
    public static void main(String[] argv) {
        File tmpDir = new File(System.getProperty("user.dir") + File.separator + "tmp" + File.separator);
        tmpDir.mkdirs();
        
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        System.setProperty("java.io.tmpdir", tmpDir.toString());
        
        MainGui.main(argv);
    }
}