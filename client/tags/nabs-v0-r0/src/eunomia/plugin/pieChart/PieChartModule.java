/*
 * PieChartModule.java
 *
 * Created on June 24, 2005, 4:14 PM
 *
 */

package eunomia.plugin.pieChart;

import eunomia.plugin.interfaces.*;
import eunomia.config.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import javax.swing.*;
import eunomia.core.data.flow.*;
import eunomia.core.data.streamData.StreamDataSource;
import eunomia.util.Util;
import java.awt.Font;
import org.jfree.chart.title.TextTitle;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PieChartModule implements Module, ConfigChangeListener, RefreshNotifier {
    private StreamPieData spd;
    private JFreeChart dataChart;
    private JComponent chartComp;
    private String title;
    private StreamDataSource ds;
    
    public PieChartModule() {
        spd = new StreamPieData();
        dataChart = ChartFactory.createPieChart(null, spd, false, false, false);
        spd.setFilter(new Filter());
        
        PiePlot plot = (PiePlot)dataChart.getPlot();
        plot.setLabelGenerator(spd);
        
        configurationChanged();
        chartComp = new ChartPanel(dataChart, true);
        Settings.addConfigChangeListener(this);
    }
    
    public ModularFlowProcessor getFlowPocessor(){
        return spd;
    }
    
    public JComponent getJComponent(){
        return chartComp;
    }
    
    public RefreshNotifier getRefreshNotifier(){
        return this;
    }

    public void configurationChanged() {
        PiePlot plot = (PiePlot)dataChart.getPlot(); 
        for(int i = 0; i < Flow.NUM_TYPES; i++){
            plot.setSectionPaint(i, Settings.getTypeColor(i));
        }
    }

    public void updateData() {
        if(chartComp.isShowing()){
            spd.updateData();
        }
    }

    public JComponent getControlComponent() {
        return null;
    }

    public boolean allowFullscreen() {
        return true;
    }

    public boolean allowFilters() {
        return true;
    }

    public boolean isConfigSeparate() {
        return false;
    }

    public boolean allowToolbar() {
        return true;
    }

    public boolean isControlSeparate() {
        return false;
    }

    public void showLegend(boolean b) {
    }

    public void showTitle(boolean b) {
    }

    public String getTitle() {
        if(title == null){
            return "Network View";
        }
        
        return title.replace('\n', ' ');
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public void start() {
    }

    public void stop() {
    }

    public void reset() {
        spd.reset();
        setGenTitle();
    }
    
    private void setGenTitle(){
        StringBuilder builder = new StringBuilder();
        if(ds != null){
            builder.append("View From ");
            builder.append(ds.toString());
        }
        builder.append("\nSince: ");
        builder.append(Util.getTimeStamp(System.currentTimeMillis(), true, true));
        
        title = builder.toString();
        Font font = new Font("Verdana", Font.BOLD, 12);
        dataChart.setTitle(new TextTitle(title, font));
    }

    public Filter getFilter() {
        return spd.getFilter();
    }

    public void setStream(StreamDataSource sds) {
        ds = sds;
        setGenTitle();
    }
}