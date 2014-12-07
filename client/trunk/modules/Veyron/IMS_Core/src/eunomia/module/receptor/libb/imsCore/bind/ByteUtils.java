/*
 * ByteUtils.java
 *
 * Created on January 27, 2008, 11:11 AM
 *
 */

package eunomia.module.receptor.libb.imsCore.bind;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ByteUtils {
    public static final int LONG_SIZE = 8;
    public static final int INT_SIZE = 4;
    public static final int CHAR_SIZE = 2;
    public static final int SHORT_SIZE = 2;
    
    public static long bytesToLong(byte[] arr, int offset) {
        long num = 
              (long)( (arr[offset + 0] & 0xFF) << 56) |
              (long)( (arr[offset + 1] & 0xFF) << 48) |
              (long)( (arr[offset + 2] & 0xFF) << 40) |
              (long)( (arr[offset + 3] & 0xFF) << 32) |
              (long)( (arr[offset + 4] & 0xFF) << 24) |
              (long)( (arr[offset + 5] & 0xFF) << 16) |
              (long)( (arr[offset + 6] & 0xFF) <<  8) |
              (long)( (arr[offset + 7] & 0xFF) <<  0);
        
        return num;
    }
    
    public static int longToBytes(byte[] arr, int offset, long num) {
        arr[offset + 0] = (byte)(num >> 56);
        arr[offset + 1] = (byte)(num >> 48);
        arr[offset + 2] = (byte)(num >> 40);
        arr[offset + 3] = (byte)(num >> 32);
        arr[offset + 4] = (byte)(num >> 24);
        arr[offset + 5] = (byte)(num >> 16);
        arr[offset + 6] = (byte)(num >> 8);
        arr[offset + 7] = (byte)(num >> 0);
        
        return 8;
    }
    
    public static long[] bytesToLongArr(byte[] arr, int offset, long[] num, int soff, int size) {
        for (int i = 0; i < size; ++i) {
            int off = offset + (i * LONG_SIZE);
            
            num[soff + i] = 
              (long)( (arr[off + 0] & 0xFF) << 56) |
              (long)( (arr[off + 1] & 0xFF) << 48) |
              (long)( (arr[off + 2] & 0xFF) << 40) |
              (long)( (arr[off + 3] & 0xFF) << 32) |
              (long)( (arr[off + 4] & 0xFF) << 24) |
              (long)( (arr[off + 5] & 0xFF) << 16) |
              (long)( (arr[off + 6] & 0xFF) <<  8) |
              (long)( (arr[off + 7] & 0xFF) <<  0);
        }
        
        return num;
    }
    
    public static int longArrToBytes(byte[] arr, int offset, long[] num, int soff, int size) {
        for (int i = 0; i < size; ++i) {
            long val = num[soff + i];
            int off = offset + (i * LONG_SIZE);
            
            arr[off + 0] = (byte)(val >> 56);
            arr[off + 1] = (byte)(val >> 48);
            arr[off + 2] = (byte)(val >> 40);
            arr[off + 3] = (byte)(val >> 32);
            arr[off + 4] = (byte)(val >> 24);
            arr[off + 5] = (byte)(val >> 16);
            arr[off + 6] = (byte)(val >> 8);
            arr[off + 7] = (byte)(val >> 0);
        }
        
        return LONG_SIZE * size;
    }
    
    public static short bytesToShort(byte[] arr, int offset) {
        int num = 
              (short)( (arr[offset + 0] & 0x00FF) << 8) |
              (short)( (arr[offset + 1] & 0x00FF));
        
        return (short)num;
    }
    
    public static int shortToBytes(byte[] arr, int offset, int num) {
        arr[offset + 0] = (byte)(num >> 8);
        arr[offset + 1] = (byte)(num >> 0);
        
        return INT_SIZE;
    }

    public static int bytesToInt(byte[] arr, int offset) {
        int num = 
              (int)( (arr[offset + 0] & 0xFF) << 24) |
              (int)( (arr[offset + 1] & 0xFF) << 16) |
              (int)( (arr[offset + 2] & 0xFF) <<  8) |
              (int)( (arr[offset + 3] & 0xFF) <<  0);
        
        return num;
    }
    
    public static int intToBytes(byte[] arr, int offset, int num) {
        arr[offset + 0] = (byte)(num >> 24);
        arr[offset + 1] = (byte)(num >> 16);
        arr[offset + 2] = (byte)(num >> 8);
        arr[offset + 3] = (byte)(num >> 0);
        
        return INT_SIZE;
    }
    
    public static int[] bytesToIntArr(byte[] arr, int offset, int[] num, int soff, int size) {
        for (int i = 0; i < size; ++i) {
            int off = offset + (i * INT_SIZE);
            
            num[soff + i] = 
              (int)( (arr[off + 0] & 0xFF) << 24) |
              (int)( (arr[off + 1] & 0xFF) << 16) |
              (int)( (arr[off + 2] & 0xFF) <<  8) |
              (int)( (arr[off + 3] & 0xFF) <<  0);
        }
        
        return num;
    }
    
    public static int intArrToBytes(byte[] arr, int offset, int[] num, int soff, int size) {
        for (int i = 0; i < size; ++i) {
            int val = num[soff + i];
            int off = offset + (i * INT_SIZE);
            
            arr[off + 0] = (byte)(val >> 24);
            arr[off + 1] = (byte)(val >> 16);
            arr[off + 2] = (byte)(val >> 8);
            arr[off + 3] = (byte)(val >> 0);
        }
        
        return INT_SIZE * size;
    }
    
    public static int charToBytes(byte[] arr, int offset, char c) {
        arr[offset + 0] = (byte) ((int) (c & 0xFF00) >> 8);
        arr[offset + 1] = (byte) (c & 0xFF);
        
        return 2;
    }

    public static char bytesToChar(byte[] arr, int offset) {
        return (char) ((arr[offset] << 8) | arr[offset + 1]);
    }
}