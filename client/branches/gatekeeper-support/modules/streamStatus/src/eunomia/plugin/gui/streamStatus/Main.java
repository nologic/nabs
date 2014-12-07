/*
 * StreamStatusBar.java
 *
 * Created on June 14, 2005, 8:23 PM
 */

package eunomia.plugin.gui.streamStatus;

import eunomia.flow.Filter;
import eunomia.messages.Message;
import com.vivic.eunomia.module.frontend.GUIModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.util.Util;
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
public class Main extends JPanel implements GUIModule {
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
    
    public Filter getFilter(){
        return null;
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
    
    public void setReceptor(ConsoleReceptor rec){
        receptor = rec;
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
    
    public void messageResponse(Message msg) {
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