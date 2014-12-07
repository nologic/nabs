/*
 * Exporter.java
 *
 * Created on August 19, 2005, 6:50 PM
 *
 */

package eunomia.core.data.staticData.exporter;

import eunomia.core.data.staticData.*;
import java.io.IOException;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface Exporter {
    public void export(DiskResultSet drs, String file) throws IOException;
    public String getFileExtention();
}
