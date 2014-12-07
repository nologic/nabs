/*
 * DistroGraph.java
 *
 * Created on July 19, 2007, 10:12 PM
 *
 */

package eunomia.plugin.gui.networkPolicy.alert;

import eunomia.receptor.module.NABFlow.NABFlow;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DistroGraphData implements CategoryDataset, CategoryItemLabelGenerator, CategoryToolTipGenerator {
    private List list;
    private DatasetChangeEvent event;
    private long[] data;
    private List cKeys;
    private DatasetGroup dg;
    
    public DistroGraphData() {
        cKeys = new LinkedList();
        data = new long[NABFlow.NUM_TYPES];
        list = new LinkedList();
        event = new DatasetChangeEvent(this, this);
        
        cKeys.add("Bytes");
    }
    
    public void updateData(long[] newData){
        System.arraycopy(newData, 0, data, 0, data.length);
        Iterator it = list.iterator();

        while(it.hasNext()){
            DatasetChangeListener l = (DatasetChangeListener)it.next();
            l.datasetChanged(event);
        }
    }

    public Comparable getRowKey(int row) {
        return NABFlow.typeNames[row];
    }

    public int getRowIndex(Comparable key) {
        for(int i = 0; i < NABFlow.NUM_TYPES; i++){
            if(key.compareTo(NABFlow.typeNames[i]) == 0){
                return i;
            }
        }
        
        return -1;
    }

    public List getRowKeys() {
        return NABFlow.typeNamesList;
    }

    public Comparable getColumnKey(int column) {
        return (Comparable)cKeys.get(column);
    }

    public int getColumnIndex(Comparable key) {
        return 0;
    }

    public List getColumnKeys() {
        return cKeys;
    }

    public Number getValue(Comparable rowKey, Comparable column) {
        return getValue(getRowIndex(rowKey), 0);
    }

    public int getRowCount() {
        return data.length;
    }

    public int getColumnCount() {
        return 1;
    }

    public Number getValue(int row, int column) {
        return Long.valueOf(data[row]);
    }

    public void addChangeListener(DatasetChangeListener l) {
        list.add(l);
    }

    public void removeChangeListener(DatasetChangeListener l) {
        list.remove(l);
    }

    public DatasetGroup getGroup() {
        return dg;
    }

    public void setGroup(DatasetGroup group) {
        dg = group;
    }

    public String generateRowLabel(CategoryDataset dataset, int row) {
        return "";
    }

    public String generateColumnLabel(CategoryDataset dataset, int column) {
        return "";
    }

    public String generateLabel(CategoryDataset dataset, int row, int column) {
        return "";
    }

    public String generateToolTip(CategoryDataset dataset, int row, int column) {
        return "";
    }
    
}
