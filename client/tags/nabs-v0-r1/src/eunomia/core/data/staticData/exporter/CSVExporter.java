/*
 * CSVExporter.java
 *
 * Created on August 19, 2005, 6:51 PM
 *
 */

package eunomia.core.data.staticData.exporter;

import eunomia.core.data.staticData.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */

public class CSVExporter implements Exporter {
    private static byte[] fieldSeperator;
    private static byte[] lineSeperator;

    private int exported;
    
    static {
        fieldSeperator = ", ".getBytes();
        lineSeperator = System.getProperty("line.separator").getBytes();
    }
    
    public CSVExporter() {
    }
    
    public void export(DiskResultSet drs, String file) throws IOException {
        exported = 0;

        byte[] fSep = fieldSeperator;
        byte[] lSep = lineSeperator;
        
        FileOutputStream fout = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        
        int cols = drs.getColumnCount();
        for(int i = 0; i < cols; i++){
            bout.write(drs.getColumnName(i).getBytes());
            if(i < cols - 1){
                bout.write(fSep);
            } else {
                bout.write(lSep);
            }
        }
        
        Iterator it = drs.getRowIterator();
        while(it.hasNext()){
            Object[] fields = (Object[])it.next();
            
            for(int i = 0; i < fields.length; i++){
                bout.write(fields[i].toString().getBytes());
                if(i < fields.length - 1){
                    bout.write(fSep);
                } else {
                    bout.write(lSep);
                }
            }
            exported++;
        }
        
        bout.close();
        fout.close();
    }
    
    public int getExportedCount(){
        return exported;
    }
    
    public String getFileExtention(){
        return "csv";
    }
}