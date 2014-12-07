/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.frontend.proc.NeoDB;

import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author justin
 */
public class Main extends JPanel implements FrontendProcessorModule {
    public Main() {
    }
    
    public JComponent getJComponent() {
        return this;
    }

    public JComponent getControlComponent() {
        return null;
    }

    public String getTitle() {
        return "Test Neoflow DB Processor";
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
