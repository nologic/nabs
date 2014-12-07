package eunomia.module;

import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ReportingFrontendModule {
    //part getters.
    public void processMessage(DataInputStream din) throws IOException;
    
    //properties
    public void setProperty(String name, Object value);
    public Object getProperty(String name);
    
    //Communications
    public void updateStatus(InputStream in) throws IOException;
    public void getControlData(OutputStream out) throws IOException;
    public void setControlData(InputStream in) throws IOException;
}