/*
 * StreamCategoryData.java
 *
 * Created on June 8, 2005, 5:16 PM
 */

package eunomia.plugin.gui.lossyHistogram;

import java.util.*;
import eunomia.util.*;
import eunomia.plugin.interfaces.*;
import eunomia.plugin.alg.*;
        
import org.jfree.data.category.*;
import org.jfree.data.general.*;
import org.jfree.chart.labels.CategoryItemLabelGenerator;

import org.apache.log4j.*;
import eunomia.util.number.*;
import org.jfree.chart.labels.*;
import eunomia.receptor.module.NABFlow.NABFlow;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class StreamCategoryData implements CategoryDataset, CategoryItemLabelGenerator, CategoryToolTipGenerator {
    public static final int SRC_IP = 0, DST_IP = 1, HOST_TO_HOST = 2, PER_FLOW = 3, SINGLE_HOST = 4;
    
    private DatasetGroup dg;
    private List list;
    private List colKeys;
    private DatasetChangeEvent event;
    private LCTableEntry[] lossyTable;
    private int[] lastFrequency;
    private boolean[] changeTable;
    private int flowCount;
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
    
    public void updateData(){
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
            if(key.compareTo(Util.ipToString(new StringBuilder(), lossyTable[i].getFlow().getSourceIP())) == 0){
                return i;
            }
        }
        
        return -1;
    }
    
    public LCTableEntry getTableEntry(int entry){
        return lossyTable[entry];
    }
    
    private StringBuilder sb = new StringBuilder();
    public Comparable getColumnKey(int column) {
        if(lossyTable[column] == null){
            return "";
        }
        if(sb.length() > 0){
            sb.delete(0, sb.length());
        }
        
        NABFlow flow = lossyTable[column].getFlow();
        switch(type){
            case SINGLE_HOST:
            case SRC_IP: 
                return Util.ipToString(sb, flow.getSourceIP()).toString();
            
            case DST_IP: 
                return Util.ipToString(sb, flow.getDestinationIP()).toString();
            
            case HOST_TO_HOST:
                Util.ipToString(sb, flow.getSourceIP()).append(" - ");
                return Util.ipToString(sb, flow.getDestinationIP()).toString();
                
            case PER_FLOW:
                Util.ipToString(sb, flow.getSourceIP()).append(":" + flow.getSourcePort()).append(" - ");
                Util.ipToString(sb, flow.getDestinationIP()).append(":" + flow.getDestinationPort());
                return sb.toString();
                
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
        
        return NABFlow.NUM_TYPES;
    }
    
    public int getRowIndex(Comparable key) {
        for(int i = 0; i < NABFlow.NUM_TYPES; i++){
            if(key.compareTo(NABFlow.typeNames[i]) == 0){
                return i;
            }
        }
        
        return -1;
    }
    
    public Comparable getRowKey(int row) {
        return NABFlow.typeNames[row];
    }
    
    public List getRowKeys() {
        return NABFlow.typeNamesList;
    }
    
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        return getValue(0, getColumnIndex(columnKey));
    }
    
    public Number getValue(int row, int column) {
        long val = 0;
        long size = 0;
        LCTableEntry f = lossyTable[column];
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
        LCTableEntry entry = lossyTable[column];
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
        Util.getTimeStamp(builder, entry.getStartTime(), true, true);
        
        builder.append(", Idle: ");
        builder.append(Integer.toString(entry.getIdleTime()));
        
        return builder.toString();
    }
 
    public void updateLossyTable(LCTableEntry[] table) {
        lossyTable = table;
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
        if(t > -1 || t < 5){
            type = t;
        }
    }
    
    public int getType(){
        return type;
    }
}