/*
 * HistoryData.java
 *
 * Created on August 11, 2005, 6:56 PM
 *
 */

package eunomia.plugin.hostView;

import eunomia.core.data.flow.*;
import eunomia.plugin.interfaces.*;
import eunomia.util.number.*;
import java.util.*;
import org.apache.log4j.*;
import org.jfree.chart.axis.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HistoryData implements CategoryDataset, RefreshNotifier {
    private DatasetGroup dg;
    private DatasetChangeEvent event;
    private List list;
    private int[][] data;
    private int colCount;
    private List colKeys;
    private int maxCount;
    private ModInteger myInt;
    
    private static Logger logger;
    private static List rowKeys;
    
    static {
        logger = Logger.getLogger(HistoryData.class);
        rowKeys = new LinkedList();
        for(int i = 0; i < Flow.NUM_TYPES; ++i){
            rowKeys.add(Integer.toString(i));
        }
    }
    
    public HistoryData(int maxEntries) {
        list = new LinkedList();
        data = new int[maxEntries][];
        colCount = 0;
        colKeys = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        maxCount = maxEntries;
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
        return Flow.NUM_TYPES;
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
    
    public void addEntryCopy(int[] entry){
        if(colCount < maxCount){
            int[] entrycp = new int[entry.length];
            System.arraycopy(entry, 0, entrycp, 0, entry.length);
            System.arraycopy(data, 0, data, 1, colCount);
            data[0] = entrycp;
            ++colCount;
        } else {
            int[] oldEntry = data[data.length - 1];
            System.arraycopy(data, 0, data, 1, data.length - 1);
            System.arraycopy(entry, 0, oldEntry, 0, entry.length);
            data[0] = oldEntry;
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