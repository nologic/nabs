/*
 * StreamStatusBar.java
 *
 * Created on June 14, 2005, 8:23 PM
 */

package eunomia.plugin.gui.streamStatus;

import eunomia.flow.Filter;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.core.receptor.Receptor;
import eunomia.messages.Message;
import eunomia.messages.module.msg.ModuleControlDataMessage;
import eunomia.messages.module.msg.ModuleStatusMessage;
import eunomia.plugin.interfaces.GUIModule;
import eunomia.util.Util;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.*;
import java.text.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class Main extends JPanel implements GUIModule, MessageReceiver {
    private static Format form;
    private static Font labelFont;

    private JLabel dataRate;
    private JLabel dataTotal;
    private JLabel eventRate;
    private JLabel eventTotal;
    private Receptor receptor;
    
    private StringBuilder builder;
    
    static {
        form = DecimalFormat.getInstance();
        labelFont = new Font("SansSerif", Font.PLAIN, 9);
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
    
    public Filter getFilter(){
        return null;
    }
    
    public MessageReceiver getReceiver(){
        return this;
    }
    
    //module options
    public boolean allowFullscreen(){
        return true;
    }
    
    public boolean allowFilters(){
        return true;
    }
    
    public boolean allowToolbar(){
        return true;
    }
    
    public boolean isControlSeparate(){
        return true;
    }
    
    //module settings
    public void showLegend(boolean b){
    }
    
    public void showTitle(boolean b){
    }
    
    public void setReceptor(Receptor rec){
        receptor = rec;
    }
    
    public Receptor getReceptor(){
        return receptor;
    }
    
    //properties
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name){
        return null;
    }
    
    //Communications
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        if(builder.length() != 0){
            builder.delete(0, builder.length());
        }
        
        eventTotal.setText(form.format(new Long(din.readLong())));
        dataTotal.setText(Util.convertBytes(builder, din.readLong(), true).toString());
        builder.delete(0, builder.length());
        dataRate.setText(Util.convertBytesRate(builder, din.readDouble(), true).toString());
        eventRate.setText(form.format(new Integer((int)din.readDouble())) + "e/s");
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }
    
    public void setControlData(InputStream in) throws IOException {
    }
    
    public void messageResponse(Message msg) {
    }
    
    private void addControls(){
        setLayout(new GridLayout(4, 2));
        
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
        
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    }
    
    private JLabel makeLabel(String str){
        JLabel label = new JLabel(str);
        
        label.setFont(labelFont);
        
        return label;
    }
}