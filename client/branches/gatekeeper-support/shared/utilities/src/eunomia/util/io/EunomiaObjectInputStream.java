/*
 * tmpOO.java
 *
 * Created on August 5, 2007, 9:30 PM
 *
 */

package eunomia.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EunomiaObjectInputStream extends ObjectInputStream {
    private static ClassLoader loader;
    
    public EunomiaObjectInputStream(InputStream in) throws IOException {
        super(in);
    }
    
    protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(desc);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return loader.loadClass(desc.getName());
    }

    public static void setClassLoader(ClassLoader cl) {
        if(loader == null) {
            loader = cl;
        }
    }
}