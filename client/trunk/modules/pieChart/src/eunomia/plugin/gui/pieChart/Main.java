/*
 * PieChartModule.java
 *
 * Created on June 24, 2005, 4:14 PM
 *
 */

package eunomia.plugin.gui.pieChart;

import com.vivic.eunomia.sys.frontend.ConsoleContext;
import eunomia.config.ConfigChangeListener;
import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import com.vivic.eunomia.sys.frontend.GlobalSettings;
import eunomia.receptor.module.NABFlow.NABFlow;
import com.vivic.eunomia.sys.util.Util;
import eunomia.util.number.ModDouble;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.PieDataset;

/**
 * This is the main class used by the GUI to instantiate the module. The class name
 * will be generated by concatenating the prefix "eunomia.plugin.gui.", the name 
 * of the module (pieChart in this case) and postfix ".Main"
 * @author Mikhail Sosonkin
 */
public class Main implements FrontendProcessorModule, ConfigChangeListener, PieSectionLabelGenerator, PieDataset {
    /**
     * This module uses the JFreeChart library to render the graphics. This is a field
     * that maintains the complete description of the chart.
     */
    private JFreeChart dataChart;
    
    /**
     * The component that is actually displayed it to the user. This is the object
     * that is returned by getJComponent() method.
     */
    private JComponent chartComp;
    
    /**
     * GUI compoment that contains the configuration component. It allows the user
     * to set the aging coefficient.
     */
    private JComponent configComp;
    
    /**
     * The title on the chart. Contained in the chartComp
     */
    private String title;
    
    /**
     * Needed by the PieDataset interface, has no real purpose for this module.
     */
    private DatasetGroup dg;
    
    /**
     * List of change listeners. Used to notify the chart when there is a change.
     */
    private List list;
    
    /**
     * On an event (usually an update from the middleware) list is traversed and this
     * Object is sent to the chart.
     */
    private DatasetChangeEvent event;
    
    /**
     * The object used to represent one number for the Pie Chart. Since the JFreeChart
     * obtains one number at the time, this object can be reused for each number.
     */
    private ModDouble modDouble;
    
    /**
     * String Builder used to generate Strings, primarily to make the title.
     */
    private StringBuilder builder;
    
    /**
     * Contains data from the middleware, this is the total count of each flow type.
     */
    private long[] counts;
    
    /**
     * Same as the counts array, except the aging coefficient was applied.
     */
    private double[] percents;
    
    /**
     * Total count of flows seen by the module. Also from the middleware.
     */
    private long total;
    
    /**
     * The time in milliseconds of the last reset of the module. As recorded by the
     * middleware side.
     */
    private long lastReset;
    
    /**
     * The total aged value. Recorded by the middleware side.
     */
    private double totalPercent;
    
    /**
     * Front-end receptor associated with the module.
     */
    private ConsoleReceptor receptor;
    
    /**
     * Aging coefficient GUI component for the user to enter the value.
     */
    private JTextField acText;
    private GlobalSettings gSet;
    
    /**
     * 
     */
    public Main() {
        // Generate the configuration component.
        configComp = makeConfig();
        
        // initialize remote data.
        counts = new long[NABFlow.NUM_TYPES];
        percents = new double[NABFlow.NUM_TYPES];
        
        // utility objects.
        list = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        modDouble = new ModDouble();
        builder = new StringBuilder();

        // create the pie chart.
        dataChart = ChartFactory.createPieChart(null, this, false, false, false);
        
        // set the label renderer for the chart. Enables the module to show custom
        // text for each section.
        PiePlot plot = (PiePlot)dataChart.getPlot();
        plot.setLabelGenerator(this);
        
        // Execute function for changes of the configuration (global configuration)
        // to set current flow colors. i.e. black for text, etc.
        chartComp = new ChartPanel(dataChart, true);

        receptor = ConsoleContext.getReceptor();
        
        gSet = receptor.getGlobalSettings();
        gSet.addConfigChangeListener(this);
        
        configurationChanged();
    }
    
    private JComponent makeConfig(){
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        
        // We just want a panel with a single text field for changing the coefficient
        // value.
        topPanel.add(new JLabel("Aging coefficient: "));
        topPanel.add(acText = new JTextField("1.0"));
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // The rest of the work will be done by the front-end GUI.
        return panel;
    }
    
    /**
     * The component will be rendered on the screen.
     * @return the GUI component that will displayed to the user. It will contain the 
     * information that the module has collected.
     */
    public JComponent getJComponent(){
        return chartComp;
    }

    public void configurationChanged() {
        // change all the colors
        PiePlot plot = (PiePlot)dataChart.getPlot(); 
        for(int i = 0; i < NABFlow.NUM_TYPES; i++){
            plot.setSectionPaint(i, gSet.getTypeColor(i));
        }
    }

    /**
     * The component will be rendered on the screen when the user presses the control
     * button.
     * @return the GUI component to be used for changing module configuration.
     */
    public JComponent getControlComponent() {
        return configComp;
    }

    /**
     * Module title displayed to the user.
     * @return Title string
     */
    public String getTitle() {
        if(title == null){
            return "Network View";
        }
        
        return title.replace('\n', ' ');
    }

    /**
     * Changes module properties. Primarily used for intermodule communication.
     * @param name The property or command name.
     * @param value Target value
     */
    public void setProperty(String name, Object value) {
        // this module has no property settings.
    }

    /**
     * 
     * @param name Property name
     * @return The value for the property, does not have to be the same as the one set by the
     * set property method.
     */
    public Object getProperty(String name) {
        if(name.equals("web")) {
            BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            dataChart.draw(img.createGraphics(), new Rectangle2D.Double(0, 0, 800, 600));
            return img;
        }
        
        return null;
    }
    
    private void setGenTitle(){
        StringBuilder builder = new StringBuilder();
        
        if(receptor != null) {
            // Obtains the receptor name
            builder.append("View From ");
            builder.append(receptor.toString());
            builder.append("\n");
        }

        // and the time the collection last started.
        builder.append("Since: ");
        builder.append(Util.getTimeStamp(lastReset, true, true));
        
        // set the title on the chart
        title = builder.toString();
        Font font = new Font("Verdana", Font.BOLD, 12);
        dataChart.setTitle(new TextTitle(title, font));
    }

    /**
     * This method will write the module configuration data to the OutputStream. This 
     * will be sent to the middleware.
     * @param out Output stream that the module will write the data to.
     * @throws java.io.IOException 
     */
    public void getControlData(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        
        // write out the aging coefficient.
        dout.writeDouble(Double.parseDouble(acText.getText()));
    }

    /**
     * This method will receive the configuration data from the middleware part of the 
     * module.
     * @param in contains the new data, the module should read it with an assuption that it is
     * formatted by middleware side.
     * @throws java.io.IOException 
     */
    public void setControlData(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        
        // set the aging coefficient from the stream. 
        acText.setText(Double.toString(din.readDouble()));
    }

    /**
     * This method will receive the data from the middleware part of the module.
     * @param in stream that contains the formatted data.
     * @throws java.io.IOException 
     */
    public void updateStatus(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(in);
        
        // read the last reset time
        lastReset = din.readLong();
        
        // reset totals.
        total = 0;
        totalPercent = 0.0;
        
        // iterate and record each flow type's value
        for(int i = counts.length - 1; i != -1; --i){
            double p = din.readDouble();
            long c = din.readLong();
            
            percents[i] = p;
            counts[i] = c;
            total += c;
            totalPercent += p;
        }
        
        // Once the update is received the graphics and title need to be updated.
        updateData();
        setGenTitle();
    }

    /**
     * Generate the label
     */
    public String generateSectionLabel(PieDataset dataset, Comparable key) {
        int index = getIndex(key);
        builder.delete(0, builder.length());
        
        builder.append(key.toString());
        builder.append(" = ");
        
        if(index > -1){
            double per = percents[index];
            Util.convertBytes(builder, counts[index], true);
            
            //get a trunkated percentage.
            double percent = per/totalPercent;
            percent *= 10000.0;
            percent = (int)percent;
            percent /= 100.0;
            
            builder.append(" (");
            builder.append(Double.toString(percent));
            builder.append("%)");
            return builder.toString();
        }
        
        return "";
    }

    public int getIndex(Comparable key) {
        // This module is NABFlow specific
        for(int i = 0; i < NABFlow.typeNames.length; i++){
            if(key.compareTo(NABFlow.typeNames[i]) == 0)
                return i;
        }
        
        return -1;
    }
    
    public int getItemCount() {
        return percents.length;
    }
    
    public Comparable getKey(int index) {
        // each section of the module is a flow type.
        return NABFlow.typeNames[index];
    }
    
    public List getKeys() {
        return NABFlow.typeNamesList;
    }
    
    public Number getValue(int item) {
        // as mentioned earlier we can reuse the object.
        modDouble.setDouble(percents[item]);
        return modDouble;
    }
    
    public Number getValue(Comparable key) {
        return getValue(getIndex(key));
    }

    public void setGroup(DatasetGroup group) {
        dg = group;
    }
    
    public DatasetGroup getGroup() {
        return dg;
    }
    
    public void addChangeListener(DatasetChangeListener l) {
        list.add(l);
    }
    
    public void removeChangeListener(DatasetChangeListener l) {
        list.remove(l);
    }
    
    public void updateData(){
        // The component should be notified only if the user actually sees it.
        if(chartComp.isShowing()){
            Iterator it = list.iterator();

            while(it.hasNext()){
                DatasetChangeListener l = (DatasetChangeListener)it.next();
                l.datasetChanged(event);
            }
        }
    }

    public AttributedString generateAttributedSectionLabel(PieDataset pieDataset, Comparable comparable) {
        return null;
    }
    
    public void processMessage(DataInputStream din) throws IOException {
    }
}