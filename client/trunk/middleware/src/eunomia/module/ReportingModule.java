package eunomia.module;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReportingModule {
    public void updateStatus(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    
    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException;
}