/*
 * StreamCategoryData.java
 *
 * Created on June 8, 2005, 5:16 PM
 */

package eunomia.plugin.lossyHistogram;

import java.util.*;
import eunomia.util.*;
import eunomia.plugin.interfaces.*;
import eunomia.plugin.alg.*;
        
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import org.jfree.chart.labels.CategoryItemLabelGenerator;

import org.apache.log4j.*;
import eunomia.core.data.flow.*;
import eunomia.util.number.*;
import org.jfree.chart.labels.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamCategoryData implements CategoryDataset, RefreshNotifier, 
        CategoryItemLabelGenerator, CategoryToolTipGenerator {
    public static final int SRC_IP = 0, DST_IP = 1, HOST_TO_HOST = 2, PER_FLOW = 3;
    
    private DatasetGroup dg;
    private List list;
    private List colKeys;
    private DatasetChangeEvent event;
    private TableEntry[] lossyTable;
    private int[] lastFrequency;
    private boolean[] changeTable;
    private int flowCount;
    private LossyCounter lossy;
    private ModLong modLong;
    private int type;
    private StringBuilder builder;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(StreamCategoryData.class);
    }
    
    public StreamCategoryData() {
        builder = new StringBuilder();
        list = new LinkedList();
        colKeys = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        modLong = new ModLong();
        type = SRC_IP;
    }
    
    public void setLossyCounter(LossyCounter l){
        lossy = l;
    }
    
    public void updateData(){
        updateLossyTable();
        if(lossyTable != null){
            Iterator it = list.iterator();

            synchronized(lossyTable){
                while(it.hasNext()){
                    DatasetChangeListener l = (DatasetChangeListener)it.next();
                    l.datasetChanged(event);
                }
            }
        }
    }
    
    public void addChangeListener(DatasetChangeListener l) {
        list.add(l);
    }
    
    public int getColumnCount() {
        return flowCount;
    }
    
    public int getColumnIndex(Comparable key) {
        for(int i = 0; i < flowCount; i++){
            if(key.compareTo(lossyTable[i].getFlow().getSourceAddress().getHostAddress()) == 0){
                return i;
            }
        }
        
        return -1;
    }
    
    public TableEntry getTableEntry(int entry){
        return lossyTable[entry];
    }
    
    public Comparable getColumnKey(int column) {
        if(lossyTable[column] == null){
            return "";
        }
        
        Flow flow = lossyTable[column].getFlow();
        switch(type){
            case SRC_IP: 
                return flow.getSourceAddress().getHostAddress();
            
            case DST_IP: 
                return flow.getDestinationAddress().getHostAddress();
            
            case HOST_TO_HOST:
                return flow.getSourceAddress().getHostAddress() + " - " + flow.getDestinationAddress().getHostAddress();
                
            case PER_FLOW:
                return flow.getSourceAddress().getHostAddress() + ":" + flow.getSourcePort() + 
                        " - " + flow.getDestinationAddress().getHostAddress() + ":" + flow.getDestinationPort();

            default:
                return "";
        }
    }
    
    public List getColumnKeys() {
        colKeys.clear();
        if(lossyTable != null){
            for(int i = 0; i < flowCount; i++){
                colKeys.add(getColumnKey(i));
            }
        }
        
        return colKeys;
    }
    
    public DatasetGroup getGroup() {
        return dg;
    }
    
    public int getRowCount() {
        if(lossyTable == null){
            return 0;
        }
        
        return Flow.NUM_TYPES;
    }
    
    public int getRowIndex(Comparable key) {
        for(int i = 0; i < Flow.NUM_TYPES; i++){
            if(key.compareTo(Flow.typeNames[i]) == 0){
                return i;
            }
        }
        
        return -1;
    }
    
    public Comparable getRowKey(int row) {
        return Flow.typeNames[row];
    }
    
    public List getRowKeys() {
        return Flow.typeNamesList;
    }
    
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        return getValue(0, getColumnIndex(columnKey));
    }
    
    public Number getValue(int row, int column) {
        long val = 0;
        long size = 0;
        TableEntry f = lossyTable[column];
        if(f != null){
            int[] types = f.getTypeFrequencies();
            if(types != null){
                val = (long)types[row];
            }
            size = (long)f.getFlow().getSize();
        }
        modLong.setLong(val * size);
        return modLong;
    }
    
    public void removeChangeListener(DatasetChangeListener l) {
        list.remove(l);
    }
    
    public void setGroup(DatasetGroup group) {
        dg = group;
    }
    
    public String generateColumnLabel(CategoryDataset dataset, int column){
        return "";
    }

    public String generateLabel(CategoryDataset dataset, int row, int column){
        if(lossyTable[column] == null){
            return "";
        }
        
        int f = lossyTable[column].getFrequency();
        if(lastFrequency == null){
            return "";
        }
        
        return changeTable[column]?"^":"=";
    }
    public String generateRowLabel(CategoryDataset dataset, int row) {
        return "";
    }
    
    public String generateToolTip(CategoryDataset dataset, int row, int column){
        TableEntry entry = lossyTable[column];
        if(entry == null){
            return null;
        }
        if(builder.length() > 0){
            builder.delete(0, builder.length());
        }
        
        builder.append("(");
        builder.append(getColumnKey(column));
        builder.append(") ");
        builder.append(getRowKey(row));
        builder.append(" = ");
        Util.convertBytes(builder, getValue(row, column).doubleValue(), true);
        builder.append(", Start: ");
        Util.getTimeStamp(builder, entry.getStartTimeMilis(), true, true);
        
        long time = System.currentTimeMillis();
        int idle = (int)(time - entry.getTimeStamp())/1000;
        builder.append(", Idle: ");
        builder.append(Integer.toString(idle));
        
        return builder.toString();
    }
 
    private void updateLossyTable() {
        lossyTable = lossy.getTable();
        if(lossyTable == null){
            return;
        }
        
        flowCount = lossyTable.length;
        if(lastFrequency == null || lastFrequency.length != lossyTable.length){
            lastFrequency = new int[flowCount];
            changeTable = new boolean[flowCount];
        }
        
        for(int i = 0; i < flowCount; i++){
            if(lossyTable[i] != null){
                int newF = lossyTable[i].getFrequency();
                if(lastFrequency[i] < newF){
                    changeTable[i] = true;
                } else {
                    changeTable[i] = false;
                }
                lastFrequency[i] = newF;
            } else {
                lastFrequency[i] = 0;
            }
        }
    }
    
    public void setType(int t){
        if(t > -1 || t < 4){
            type = t;
        }
    }
    
    public int getType(){
        return type;
    }
}