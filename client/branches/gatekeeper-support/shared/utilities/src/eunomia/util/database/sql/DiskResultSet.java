/*
 * DiskResultSet.java
 *
 * Created on August 17, 2005, 5:18 PM
 *
 */

package eunomia.util.database.sql;

import java.io.*;
import java.nio.*;
import java.sql.*;

import eunomia.util.io.*;
import java.math.*;
import java.nio.channels.*;
import java.util.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DiskResultSet {
    private int rowCount;
    
    private String contentFile;
    private String indecesFile;
    private OutputStream contentOutput;
    private OutputStream indecesOutput;
    private CountingDataOutputStream dout;
    private BufferedOutputStream bout;
    private DataOutputStream indexDout;
    private BufferedOutputStream indexBout;
    
    private boolean readingDataset;
    private boolean randomOpen;
    private MappedByteBuffer mbb;
    private FileChannel fileChannel;
    private RandomAccessFile indexRaf;
    private RandomAccessFile dataRaf;
    
    //private header info.
    private int[] types;
    private boolean[] sign;
    private String[] names;
    
    //row
    private Object[] row;
    private int lastRow;
    
    public DiskResultSet() {
        randomOpen = false;
        readingDataset = false;
    }
    
    public void setOutputStreams(OutputStream co, OutputStream io){
        contentOutput = co;
        indecesOutput = io;
    }
    
    public void setInputFiles(String cf, String inf){
        contentFile = cf;
        indecesFile = inf;
    }
    
    private void openFile() throws IOException {
        if(randomOpen){
            closeRandomReads();
        }
        
        bout = new BufferedOutputStream(contentOutput, 65536);
        dout = new CountingDataOutputStream(bout);
        
        indexBout = new BufferedOutputStream(indecesOutput, 65536);
        indexDout = new DataOutputStream(indexBout);
    }
    
    private void closeFile() throws IOException{
        dout.flush();
        bout.flush();
        contentOutput.flush();
        dout.close();
        bout.close();
        contentOutput.close();
        
        indexDout.flush();
        indexBout.flush();
        indecesOutput.flush();
        indexDout.close();
        indexBout.close();
        indecesOutput.close();
    }
    
    public Iterator getRowIterator() throws IOException {
        return new RowIterator(contentFile, rowCount);
    }
    
    private void readHeader() throws IOException {
        FileInputStream fin = new FileInputStream(contentFile);
        DataInputStream din = new DataInputStream(fin);
        int cols = din.readInt();

        types = new int[cols];
        sign = new boolean[cols];
        names = new String[cols];
        row = new Object[cols];

        for(int i = 0; i < cols; i++){
            names[i] = readString(din);
            types[i] = din.readInt();
            if(types[i] == Types.INTEGER || types[i] == Types.BIGINT){
                sign[i] = din.readBoolean();
            }
        }
        
        din.close();
        fin.close();
    }
    
    public void prepareRandomReads() throws IOException {
        if(readingDataset){
            return;
        }
        
        if(randomOpen){
            closeRandomReads();
        }

        readHeader();
        
        try {
            dataRaf = new RandomAccessFile(contentFile, "r");
            indexRaf = new RandomAccessFile(indecesFile, "r");
            fileChannel = indexRaf.getChannel();
            mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            
            //find rowcount;
            dataRaf.seek(dataRaf.length() - 4);
            rowCount = dataRaf.readInt();
            dataRaf.seek(0);
            
            lastRow = -1;
            readingDataset = false;
            randomOpen = true;
        } catch(Exception e){
            System.out.println("Error opening random");
            e.printStackTrace();
            rowCount = 0;
        }
    }
    
    public void closeRandomReads() throws IOException {
        if(randomOpen) {
            indexRaf.close();
            fileChannel.close();
            dataRaf.close();
            mbb = null;
            System.gc();
            randomOpen = false;
        }
    }
    
    public void setRow(int row) throws IOException {
        if((lastRow != -1 && row == lastRow) || readingDataset){
            return;
        }
        
        lastRow = row;
        
        if(!randomOpen){
            prepareRandomReads();
        }
        
        row *= 8;
        
        if(row >= 0 && row <= mbb.limit() - 8){
            long filePosition = mbb.getLong(row);
            dataRaf.seek(filePosition);
            readRow();
        } else {
            System.out.println(row + ">=" + 0  +" && " + row + " <= " + mbb.limit() + " - " + 8);
        }
    }
    
    public int getRowCount(){
        return rowCount;
    }
    
    public int getColumnCount(){
        return types.length;
    }
    
    private void readRow() throws IOException {
        readRow(dataRaf, row, types, sign);
    }
    
    public int getType(int column){
        return types[column];
    }
    
    public String getColumnName(int col){
        return names[col];
    }
    
    public boolean getSigned(int column){
        return sign[column];
    }
    
    public Class getTypeClass(int column){
        int type = types[column];
        switch(type){
            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.TINYINT:
            case Types.SMALLINT:
                return Integer.class;
            case Types.INTEGER:
                if (!sign[column]) {
                    return Long.class;
                } else {
                    return Integer.class;
                }
            case Types.BIGINT:
                if(!sign[column]){
                    return BigDecimal.class;
                } else {
                    return Long.class;
                }
            case Types.REAL:
            case Types.FLOAT:
                return Float.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            default:
                return String.class;
        }
    }
       
    public Object getObject(int col) throws IOException {
        if(col < 0 || col > row.length){
            System.out.println("Column out of range: " + col + " < 0 || " + col + " > " + row.length);
            return null;
        }
        
        return row[col];
    }
    
    public void retrieveResultSet(ResultSet set) throws SQLException, IOException {
        readingDataset = true;
        rowCount = 0;
        
        if(set == null){
            types = new int[0];
            sign = new boolean[0];
            names = new String[0];
            row = new Object[0];
            readingDataset = false;
            
            return;
        }
        
        openFile();
        ResultSetMetaData data = set.getMetaData();
        
        int cols = data.getColumnCount();
        types = new int[cols];
        sign = new boolean[cols];
        names = new String[cols];
        row = new Object[cols];
        lastRow = -1;
        
        dout.writeInt(cols);
        for(int i = 0; i < cols; ++i){
            dout.writeString(names[i] = data.getColumnName(i + 1));
            dout.writeInt(types[i] = data.getColumnType(i + 1));
            if(types[i] == Types.INTEGER || types[i] == Types.BIGINT){
                dout.writeBoolean(sign[i] = data.isSigned(i + 1));
            }
        }
        
        while(set.next()){
            ++rowCount;
            
            indexDout.writeLong(dout.getCount());
            for(int i = 0; i < cols; i++){
                int index = i + 1;
                switch(types[i]){
                    case Types.BIT:
                    case Types.BOOLEAN:
                        dout.writeBoolean(set.getBoolean(index));
                        break;
                    case Types.TINYINT:
                    case Types.SMALLINT:
                        dout.writeInt(set.getInt(index));
                        break;
                    case Types.INTEGER:
                        if (!sign[i]) {
                            dout.writeLong(set.getLong(index));
                        } else {
                            dout.writeInt(set.getInt(index));
                        }
                        break;
                    case Types.BIGINT:
                        if(!sign[i]){
                            dout.writeString(set.getBigDecimal(index).toPlainString());
                        } else {
                            dout.writeLong(set.getLong(index));
                        }
                        break;
                    case Types.REAL:
                    case Types.FLOAT:
                        dout.writeFloat(set.getFloat(index));
                        break;
                    case Types.DOUBLE:
                        dout.writeDouble(set.getDouble(index));
                        break;
                    case Types.DECIMAL:
                    case Types.NUMERIC:
                    case Types.CHAR:
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    case Types.BINARY:
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                    default:
                        dout.writeString(set.getString(index));
                        break;
                }
            }
        }
        dout.writeInt(rowCount);
        readingDataset = false;
        closeFile();
    }
    
    private class RowIterator implements Iterator {
        private DataInputStream din;
        private int rows;
        private int[] types;
        private boolean[] sign;
        private int row;
        private Object[] fields;
        
        public RowIterator(String fileName, int rows) throws IOException {
            FileInputStream fin = new FileInputStream(fileName);
            din = new DataInputStream(new BufferedInputStream(fin));
            
            this.rows = rows;
            
            initialize();
        }
        
        public void initialize() throws IOException {
            int cols = din.readInt();
            
            types = new int[cols];
            sign = new boolean[cols];
            fields = new Object[cols];
            
            for(int i = 0; i < cols; i++){
                readString(din);
                types[i] = din.readInt();
                if(types[i] == Types.INTEGER || types[i] == Types.BIGINT){
                    sign[i] = din.readBoolean();
                }
            }
        }
        
        public boolean hasNext(){
            return row < rows;
        }
        
        public Object next(){
            try {
                readRow(din, fields, types, sign);
            } catch(IOException e){
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
            row++;
            
            return fields;
        }
        
        public void remove(){
        }
    }
    
    private static void readRow(RandomAccessFile raf, Object[] valsRow, int[] dTypes, boolean[] signs) throws IOException {
        for(int i = 0; i < valsRow.length; i++){
            switch(dTypes[i]){
                case Types.BIT:
                case Types.BOOLEAN:
                    valsRow[i] = Boolean.valueOf(raf.readBoolean());
                    break;
                case Types.TINYINT:
                case Types.SMALLINT:
                    valsRow[i] = Integer.valueOf(raf.readInt());
                    break;
                case Types.INTEGER:
                    if (!signs[i]) {
                        valsRow[i] = Long.valueOf(raf.readLong());
                    } else {
                        valsRow[i] = Integer.valueOf(raf.readInt());
                    }
                    break;
                case Types.BIGINT:
                    if(!signs[i]){
                        valsRow[i] = new BigDecimal(readString(raf));
                    } else {
                        valsRow[i] = Long.valueOf(raf.readLong());
                    }
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    valsRow[i] = readString(raf);
                    break;
                case Types.REAL:
                case Types.FLOAT:
                    valsRow[i] = Float.valueOf(raf.readFloat());
                    break;
                case Types.DOUBLE:
                    valsRow[i] = Double.valueOf(raf.readDouble());
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                default:
                    valsRow[i] = readString(raf);
            }
        }
    }

    private static String readString(RandomAccessFile raf) throws IOException {
        int len = raf.readInt();
        char[] chars = new char[len];

        for(int i = 0; i < len; ++i){
            chars[i] = raf.readChar();
        }
        
        String str = new String(chars);
        return str;
    }
    
    //A version for DataInputStream, Used by the Iterator.
    // Maybe later will find a better way of doing it, but it's good for now.
    private static void readRow(DataInputStream raf, Object[] valsRow, int[] dTypes, boolean[] signs) throws IOException {
        for(int i = 0; i < valsRow.length; i++){
            switch(dTypes[i]){
                case Types.BIT:
                case Types.BOOLEAN:
                    valsRow[i] = Boolean.valueOf(raf.readBoolean());
                    break;
                case Types.TINYINT:
                case Types.SMALLINT:
                    valsRow[i] = Integer.valueOf(raf.readInt());
                    break;
                case Types.INTEGER:
                    if (!signs[i]) {
                        valsRow[i] = Long.valueOf(raf.readLong());
                    } else {
                        valsRow[i] = Integer.valueOf(raf.readInt());
                    }
                    break;
                case Types.BIGINT:
                    if(!signs[i]){
                        valsRow[i] = new BigDecimal(readString(raf));
                    } else {
                        valsRow[i] = Long.valueOf(raf.readLong());
                    }
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    valsRow[i] = readString(raf);
                    break;
                case Types.REAL:
                case Types.FLOAT:
                    valsRow[i] = Float.valueOf(raf.readFloat());
                    break;
                case Types.DOUBLE:
                    valsRow[i] = Double.valueOf(raf.readDouble());
                    break;
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                default:
                    valsRow[i] = readString(raf);
            }
        }
    }

    private static String readString(DataInputStream raf) throws IOException {
        int len = raf.readInt();
        char[] chars = new char[len];

        for(int i = 0; i < len; ++i){
            chars[i] = raf.readChar();
        }
        
        String str = new String(chars);
        return str;
    }
}