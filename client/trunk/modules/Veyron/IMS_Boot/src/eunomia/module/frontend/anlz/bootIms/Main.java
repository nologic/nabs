/*
 * Main.java
 *
 * Created on December 27, 2007, 9:27 PM
 *
 */

package eunomia.module.frontend.anlz.bootIms;

import com.vivic.eunomia.module.frontend.FrontendAnalysisModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main extends JPanel implements FrontendAnalysisModule {
    private JTextArea summary;
    
    public Main() {
        setLayout(new BorderLayout());
        add(new JScrollPane(summary = new JTextArea()));
    }

    public JComponent getJComponent() {
        return this;
    }

    public JComponent getControlComponent() {
        return this;
    }

    public String getTitle() {
        return "IMS Boot";
    }

    public void processMessage(DataInputStream din) throws IOException {
    }

    public void setReceptor(ConsoleReceptor receptor) {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        StringBuilder b = new StringBuilder();
        
        b.append("Backlog: ").append(din.readInt()).append("\n");
        b.append("Commit Rate: ").append(din.readDouble()).append("\n\n");
        b.append("Total Hosts: ").append(din.readInt()).append("\n");
        b.append("Total Channels: ").append(din.readInt()).append("\n\n");
        b.append("Traversal Time: ").append(din.readInt()).append("\n");
        b.append("Traversal Rate: ").append(din.readDouble()).append("\n");
        b.append("Avg Connections in last hour: ").append(din.readDouble()).append("\n");
        
        summary.setText(b.toString());
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }
    
}