/*
 * StreamStatusBar.java
 *
 * Created on June 14, 2005, 8:23 PM
 */

package eunomia.plugin.streamStatus;
import eunomia.util.Util;

import java.awt.*;
import javax.swing.*;
import java.text.*;

import org.jfree.data.general.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamStatusBar extends JPanel implements DatasetChangeListener {
    public static final int VERTICAL = 0, HORIZONTAL = 1;
    
    private static Format form;
    private static Font labelFont;
    
    private StreamStatusModule status;
    private JLabel dataRate;
    private JLabel dataTotal;
    private JLabel eventRate;
    private JLabel eventTotal;
    private int or;
    
    private StringBuilder builder;
    
    static {
        form = DecimalFormat.getInstance();
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
    }
    
    public StreamStatusBar(int orientation) {
        or = orientation;
        builder = new StringBuilder();
        addControls();
    }

    public StreamStatusBar() {
        this(HORIZONTAL);
    }
    
    public void setOrientation(int orientation){
        or = orientation;
        if(or == HORIZONTAL){
            setLayout(new GridLayout(1, 8));
        } else {
            setLayout(new GridLayout(8, 1));
        }
        validate();
    }
    
    public int getOrientation(){
        return or;
    }
    
    public void setStreamStatus(StreamStatusModule ss){
        status = ss;
        ss.addChangeListener(this);
    }
    
    public void datasetChanged(DatasetChangeEvent event) {
        status.computeRates();
        if(builder.length() != 0){
            builder.delete(0, builder.length());
        }
        
        dataRate.setText(Util.convertBytesRate(builder, status.getByteRate(), true).toString());
        builder.delete(0, builder.length());
        dataTotal.setText(Util.convertBytes(builder, status.getBytes(), true).toString());
        eventRate.setText(form.format(new Integer((int)status.getEventRate())) + "e/s");
        eventTotal.setText(form.format(new Long(status.getEvents())));
    }
    
    private void addControls(){
        if(or == HORIZONTAL){
            setLayout(new GridLayout(1, 8));
        } else {
            setLayout(new GridLayout(8, 1));
        }
        
        add(makeLabel("Total Data:"));
        add(dataTotal = makeLabel("0"));
        dataTotal.setHorizontalAlignment(JLabel.CENTER);
        add(makeLabel("Data Rate: "));
        add(dataRate = makeLabel("0"));
        dataRate.setHorizontalAlignment(JLabel.CENTER);
        add(makeLabel("Total Flows:"));
        add(eventTotal = makeLabel("0"));
        eventTotal.setHorizontalAlignment(JLabel.CENTER);
        add(makeLabel("Flow Rate: "));
        add(eventRate = makeLabel("0e/s"));
        eventRate.setHorizontalAlignment(JLabel.CENTER);
    }
    
    private JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        
        return label;
    }
}