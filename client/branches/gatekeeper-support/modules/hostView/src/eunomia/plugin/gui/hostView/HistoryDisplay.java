/*
 * HistoryDisplay.java
 *
 * Created on February 27, 2006, 11:24 PM
 *
 */

package eunomia.plugin.gui.hostView;

import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.number.ModInteger;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HistoryDisplay implements CategoryDataset {
    private DatasetGroup dg;
    private DatasetChangeEvent event;
    private List list;
    private int[][] data;
    private int colCount;
    private List colKeys;
    private int maxCount;
    private ModInteger myInt;
    private byte[] ints;
    private DataInputStream dataIn;
    private ByteArrayInputStream bin;
    
    private static Logger logger;
    private static List rowKeys;
    
    static {
        logger = Logger.getLogger(HistoryDisplay.class);
        rowKeys = new LinkedList();
        for(int i = 0; i < NABFlow.NUM_TYPES; ++i){
            rowKeys.add(Integer.toString(i));
        }
    }
    
    public HistoryDisplay() {
        list = new LinkedList();
        colCount = 0;
        colKeys = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        myInt = new ModInteger();
    }
    
    public void addChangeListener(DatasetChangeListener l) {
        list.add(l);
    }
    
    public int getColumnCount() {
        if(data == null || colCount == 0){
            return 0;
        }
        return maxCount;
    }
    
    public int getColumnIndex(Comparable key) {
        try {
            return Integer.parseInt(key.toString());
        } catch(Exception e){
            return 0;
        }
    }
    
    public Comparable getColumnKey(int column) {
        return Integer.toString(column);
    }
    
    public List getColumnKeys() {
        return colKeys;
    }
    
    public DatasetGroup getGroup() {
        return dg;
    }
    
    public int getRowCount() {
        return NABFlow.NUM_TYPES;
    }
    
    public int getRowIndex(Comparable key) {
        try {
            return Integer.parseInt(key.toString());
        } catch(Exception e){
            return 0;
        }
    }
    
    public Comparable getRowKey(int row) {
        return Integer.toString(row);
    }
    
    public List getRowKeys() {
        return rowKeys;
    }
    
    public Number getValue(int row, int column) {
        int[] entry = data[maxCount - column - 1];
        if(entry == null){
            myInt.setInt(0);
        } else {
            myInt.setInt(entry[row]);
        }
        return myInt;
    }
    
    public Number getValue(Comparable rowKey, Comparable columnKey) {
        return getValue(getRowIndex(rowKey), getColumnIndex(columnKey));
    }
    
    public void removeChangeListener(DatasetChangeListener l) {
        list.remove(l);
    }
    
    public void setGroup(DatasetGroup group) {
        dg = group;
    }
    
    public void readHistory(DataInputStream din) throws IOException {
        maxCount = din.readInt();
        if(data == null){
            data = new int[maxCount][];
            for(int i = maxCount - 1; i != -1; --i){
                data[i] = new int[NABFlow.NUM_TYPES];
            }
        }
        
        colCount = din.readInt();
        int bSize = colCount * NABFlow.NUM_TYPES * 4;
        
        if(ints == null || ints.length < bSize){
            ints = new byte[colCount * NABFlow.NUM_TYPES * 4];
            dataIn = new DataInputStream(bin = new ByteArrayInputStream(ints));
        }
        bin.reset();
        din.readFully(ints, 0, bSize);
        
        for(int i = 0; i < colCount; i++){
            int[] entry = data[i];
            for(int k = 0; k < entry.length; k++){
                entry[k] = dataIn.readInt();
            }
        }
    }
    
    public void updateData() {
        Iterator it = list.iterator();
        
        while(it.hasNext()){
            DatasetChangeListener l = (DatasetChangeListener)it.next();
            l.datasetChanged(event);
        }
    }
}