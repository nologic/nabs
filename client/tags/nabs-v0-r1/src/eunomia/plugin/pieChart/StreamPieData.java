package eunomia.plugin.pieChart;
/*
 * StreamPieData.java
 *
 * Created on June 14, 2005, 5:40 PM
 */

import java.util.*;
import eunomia.plugin.interfaces.*;
import org.jfree.data.general.*;
import eunomia.util.*;

import org.apache.log4j.*;
import eunomia.core.data.flow.*;
import eunomia.util.number.*;
import org.jfree.chart.labels.PieSectionLabelGenerator;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamPieData implements PieSectionLabelGenerator, PieDataset, ModularFlowProcessor, RefreshNotifier {
    private long[] counts;
    private long total;
    private List list;
    private DatasetChangeEvent event;
    private DatasetGroup dg;
    private ModLong modLong;
    private StringBuilder builder;
    private Filter filter;

    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StreamPieData.class);
    }
    
    public StreamPieData() {
        list = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        counts = new long[Flow.NUM_TYPES];
        modLong = new ModLong();
        builder = new StringBuilder();
    }
    
    public void setGroup(DatasetGroup group) {
        dg = group;
    }
    
    public DatasetGroup getGroup() {
        return dg;
    }
    
    public void updateData(){
        Iterator it = list.iterator();
        
        while(it.hasNext()){
            DatasetChangeListener l = (DatasetChangeListener)it.next();
            l.datasetChanged(event);
        }
    }

    public void addChangeListener(DatasetChangeListener l) {
        list.add(l);
    }
    
    public void removeChangeListener(DatasetChangeListener l) {
        list.remove(l);
    }
    
    public int getIndex(Comparable key) {
        for(int i = 0; i < Flow.typeNames.length; i++){
            if(key.compareTo(Flow.typeNames[i]) == 0)
                return i;
        }
        
        return -1;
    }
    
    public int getItemCount() {
        return counts.length;
    }
    
    public Comparable getKey(int index) {
        return Flow.typeNames[index];
    }
    
    public List getKeys() {
        return Flow.typeNamesList;
    }
    
    public Number getValue(int item) {
        modLong.setLong(counts[item]);
        return modLong;
    }
    
    public Number getValue(Comparable key) {
        return getValue(getIndex(key));
    }
    
    public void reset(){
        for(int i = counts.length - 1; i != -1; --i){
            total = 0;
            counts[i] = 0;
        }
    }
    
    public void newFlow(Flow flow) {
        if(filter.allow(flow)){
            int size = flow.getSize();
            total += size;
            counts[flow.getType()] += size;
        }
    }
    
    public void setFilter(Filter f) {
        filter = f;
    }

    public Filter getFilter() {
        return filter;
    }

    public String generateSectionLabel(PieDataset dataset, Comparable key) {
        int index = getIndex(key);
        builder.delete(0, builder.length());
        
        builder.append(key.toString());
        builder.append(" = ");
        
        if(index > -1){
            long count = counts[index];
            Util.convertBytes(builder, count, true);
            double percent = (double)count/(double)total;
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
}