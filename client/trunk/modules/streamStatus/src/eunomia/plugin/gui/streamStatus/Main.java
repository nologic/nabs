/*
 * StreamStatusBar.java
 *
 * Created on June 14, 2005, 8:23 PM
 */

package eunomia.plugin.gui.streamStatus;

import com.vivic.eunomia.filter.Filter;
import com.vivic.eunomia.sys.frontend.ConsoleContext;
import eunomia.messages.Message;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import com.vivic.eunomia.sys.util.Util;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.Format;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class Main extends JPanel implements FrontendProcessorModule {
    private static Format form;
    private static Font labelFont;

    private JLabel dataRate;
    private JLabel dataTotal;
    private JLabel eventRate;
    private JLabel eventTotal;
    private ConsoleReceptor receptor;
    
    private StringBuilder builder;
    
    static {
        form = DecimalFormat.getInstance();
        labelFont = new Font("SansSerif", Font.PLAIN, 12);
    }
    
    public Main() {
        builder = new StringBuilder();
        addControls();
    }
    
    //part getters.
    public JComponent getJComponent(){
        return this;
    }
    
    public JComponent getControlComponent(){
        return null;
    }
    
    public String getTitle(){
        return null;
    }

    //properties
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name){
        if(name.equals("web")) {
            return "Total Data: " + dataTotal.getText() + " Data Rate: " + dataRate.getText() + 
                   " Total Flows: " + eventTotal.getText() + " Flow Rate: " + eventRate.getText();
        }
        return null;
    }
    
    //Communications
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);

        eventTotal.setText(form.format(new Long(din.readLong())));
        
        dataTotal.setText(Util.convertBytes(builder, din.readLong(), true).toString());
        builder.delete(0, builder.length());
        
        dataRate.setText(Util.convertBytesRate(builder, din.readDouble(), true).toString());
        builder.delete(0, builder.length());
        
        eventRate.setText(form.format(new Integer((int)din.readDouble())) + "e/s");
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }
    
    public void setControlData(InputStream in) throws IOException {
    }

    private void addControls(){
        setLayout(new GridLayout(2, 4));
        
        add(makeLabel("Total Data:"));
        add(dataTotal = makeLabel(""));
        dataTotal.setHorizontalAlignment(JLabel.CENTER);
        add(makeLabel("Data Rate:"));
        add(dataRate = makeLabel(""));
        dataRate.setHorizontalAlignment(JLabel.CENTER);
        
        add(makeLabel("Total Flows:"));
        add(eventTotal = makeLabel("0"));
        eventTotal.setHorizontalAlignment(JLabel.CENTER);
        add(makeLabel("Flow Rate: "));
        add(eventRate = makeLabel("0e/s"));
        eventRate.setHorizontalAlignment(JLabel.CENTER);
        
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }
    
    private JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        
        return label;
    }

    public void processMessage(DataInputStream din) throws IOException {
    }
}