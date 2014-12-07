/*
 * Main.java
 *
 * Created on May 31, 2005, 4:40 PM
 */

package eunomia;
import eunomia.gui.MainGui;

import java.io.*;
/**
 *
 * @author  Mikhail Sosonkin
 */
public class Main {
    /*private static FileFilter filter = new FileFilter(){
        public boolean accept(File pathname) {
            return pathname.toString().endsWith(".java") || pathname.isDirectory();
        }
    };
    public static void listFiles(File[] files){
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if(f.toString().indexOf(".svn") > 0){
                continue;
            } else {
                if(f.isDirectory()){
                    listFiles(f.listFiles(filter));
                } else {
                    System.out.println("classMap.put(\"" + f + "\", Class.forName(\"" + f + "\"));");
                }
            }
        }
    }*/
    
    public static void main(String[] argv) {
        //listFiles(new File("F:/trinetra/products/nabs/client/trunk/shared/messages/src").listFiles(filter));
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        
        MainGui.main(argv);
    }
}