/*
 * DataSource.java
 *
 * Created on June 2, 2005, 12:46 PM
 */

package eunomia.core.data;

import eunomia.core.charter.*;
import java.io.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public abstract class DataSource {
    public abstract void initiate() throws Exception;
    public abstract void terminate() throws Exception;
    public abstract void save() throws IOException;
    public abstract void load() throws IOException;
}