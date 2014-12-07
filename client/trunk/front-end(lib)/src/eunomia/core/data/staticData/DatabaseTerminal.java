/*
 * DatabaseTerminal.java
 *
 * Created on February 25, 2007, 1:56 AM
 *
 */

package eunomia.core.data.staticData;

import java.io.File;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface DatabaseTerminal {
    public void setDataset(File index, File data);
    public void lastQueryFailed();
}
