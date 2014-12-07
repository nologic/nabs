/*
 * Main.java
 *
 * Created on February 11, 2008, 8:38 PM
 *
 */

package eunomia.module.frontend.proc.exProcAnlz;

import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements FrontendProcessorModule {
    public JComponent getJComponent() {
        return new JPanel();
    }

    public JComponent getControlComponent() {
        return new JPanel();
    }

    public String getTitle() {
        return "Example";
    }

    public void processMessage(DataInputStream din) throws IOException {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public void updateStatus(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }
}