/*
 * HistoryData.java
 *
 * Created on August 11, 2005, 6:56 PM
 *
 */

package eunomia.plugin.rec.hostView;

import eunomia.flow.*;
import eunomia.plugin.interfaces.*;
import eunomia.util.number.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HistoryData {
    private int[][] data;
    private int colCount;
    private int maxCount;

    public HistoryData(int maxEntries) {
        data = new int[maxEntries][];
        colCount = 0;
        maxCount = maxEntries;
    }
    
    // change to use a circular buffer.
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
    
    public void writeOut(DataOutputStream dout) throws IOException {
        dout.writeInt(maxCount);
        dout.writeInt(colCount);
        for(int i = 0; i < colCount; i++){
            int[] entry = data[i];
            for(int k = 0; k < entry.length; k++){
                dout.writeInt(entry[k]);
            }
        }
    }
}