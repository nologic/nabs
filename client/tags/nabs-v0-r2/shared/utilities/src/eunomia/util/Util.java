/*
 * Util.java
 *
 * Created on June 22, 2005, 6:13 PM
 *
 */

package eunomia.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.security.*;
import java.util.Calendar;

/**
 *
 * @author Mikhail Sosonkin
 */

public class Util {
    private static char[][] nums256;
    private static char[] dot;
    private static String[] sizes = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
    private static String[] rates = {"B/s", "KB/s", "MB/s", "GB/s", "TB/s", "PB/s", "EB/s"};
    
    static {
        dot = ".".toCharArray();
        nums256 = new char[256][];
        for (int i = 0; i < nums256.length; i++) {
            String str = (i + "");
            nums256[i] = str.toCharArray();
        }
    }
    
    public static String convertBytes(long bytes){
        int count = 0;
        
        while(bytes > 1024.0){
            bytes >>= 10;
            ++count;
        }
        
        return new StringBuilder().append(Long.toString(bytes)).append(sizes[count]).toString();
    }
    
    public static byte[] md5(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(bytes);
    }
    
    public static StringBuilder convertBytes(StringBuilder append, double bytes, boolean truncate){
        int count = 0;
        double target = bytes;
        
        while(target > 1024.0){
            target /= 1024.0;
            ++count;
        }
        
        if(truncate){
            target *= 100.0;
            target = (double)((int)target);
            target /= 100.0;
        }
        
        return append.append(Double.toString(target)).append(sizes[count]);
    }
    
    public static String convertBytes(double bytes, boolean truncate){
        return convertBytes(new StringBuilder(), bytes, truncate).toString();
    }
    
    public static StringBuilder convertBytes(StringBuilder append, long bytes, boolean truncate){
        return convertBytes(append, (double)bytes, truncate);
    }
    
    public static String convertBytes(long bytes, boolean truncate){
        return convertBytes((double)bytes, truncate);
    }
    
    public static String convertBytesRate(double rate, boolean truncate){
        return convertBytesRate(new StringBuilder(), rate, truncate).toString();
    }
    
    public static StringBuilder convertBytesRate(StringBuilder append, double rate, boolean truncate){
        int count = 0;
        
        while(rate > 1024.0){
            rate /= 1024.0;
            ++count;
        }
        
        if(truncate){
            rate *= 100.0;
            rate = (double)((int)rate);
            rate /= 100.0;
        }
        
        return append.append(Double.toString(rate)).append(rates[count]);
    }
    
    public static int getRandomInt(String seed) throws Exception {
        SecureRandom rand;
        if(seed != null){
            rand = new SecureRandom(seed.getBytes());
        } else {
            rand = new SecureRandom();
        }
        
        return rand.nextInt();
    }
    
    private static byte[] ipWorkBytes = new byte[4];
    public static InetAddress getInetAddress(long ip){
        synchronized(ipWorkBytes){
            byte[] ipBytes = ipWorkBytes;
            long ipAdd = ip;
            
            ipBytes[0] = (byte)(ipAdd >> 24);
            ipBytes[1] = (byte)(ipAdd >> 16);
            ipBytes[2] = (byte)(ipAdd >> 8 );
            ipBytes[3] = (byte)(ipAdd      );
            try {
                return InetAddress.getByAddress(ipBytes);
            } catch(Exception e){
                return null;
            }
        }
    }
    
    public static long getLongIp(InetAddress host){
        long ip;
        long workLong1, workLong2, workLong3, workLong4;
        Long ident;
        
        byte[] buff = host.getAddress();
        workLong1 = (long)buff[0] & 0x000000FF;
        workLong1 = workLong1 << 24;
        workLong2 = (long)buff[1] & 0x000000FF;
        workLong2 = workLong2 << 16;
        workLong3 = (long)buff[2] & 0x000000FF;
        workLong3 = workLong3 << 8;
        workLong4 = (long)buff[3] & 0x000000FF;
        ip = workLong1 | workLong2 | workLong3 | workLong4;
        
        return ip;
    }
    
    private static Calendar cal = Calendar.getInstance();
    public static String getTimeStamp(long timeMilis, boolean time, boolean date){
        return getTimeStamp(new StringBuilder(), timeMilis, time, date).toString();
    }
    public static StringBuilder getTimeStamp(StringBuilder sb, long timeMilis, boolean time, boolean date){
        synchronized(cal){
            cal.setTimeInMillis(timeMilis);
            
            int f;
            if(time){
                f = cal.get(Calendar.HOUR_OF_DAY);
                if(f < 10){
                    sb.append("0");
                }
                sb.append(f);
                sb.append(":");
                f = cal.get(Calendar.MINUTE);
                if(f < 10){
                    sb.append("0");
                }
                sb.append(f);
                sb.append(":");
                f = cal.get(Calendar.SECOND);
                if(f < 10){
                    sb.append("0");
                }
                sb.append(f);
                sb.append(" ");
            }
            
            if(date){
                f = cal.get(Calendar.MONTH);
                if(f < 10){
                    sb.append("0");
                }
                sb.append(f);
                sb.append("/");
                f = cal.get(Calendar.DAY_OF_MONTH);
                if(f < 10){
                    sb.append("0");
                }
                sb.append(f);
                sb.append("/");
                sb.append(cal.get(Calendar.YEAR));
            }
            
            return sb;
        }
    }
    
    private static final char[] binMap = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
    public static String hexEncode(byte[] bytes){
        StringBuilder buff = new StringBuilder(bytes.length * 2);
        for(int i = 0; i < bytes.length; ++i){
            byte b = bytes[i];
            int b1 = (b >> 4) & 0xF;
            int b2 = (b     ) & 0xF;
            buff.append(binMap[b1]);
            buff.append(binMap[b2]);
        }
        
        return buff.toString();
    }
    
    public static byte[] hexDecode(String str){
        byte[] values;
        char[] chars = str.toCharArray();
        
        if(chars.length % 2 != 0){
            throw new IndexOutOfBoundsException("Array size invalid: " + chars.length);
        }
        
        values = new byte[chars.length / 2];
        
        for(int i = 0; i < chars.length; i += 2){
            int c1 = ((int)chars[i]) - 48;
            int c2 = ((int)chars[i + 1]) - 48;
            if(c1 > 9) c1 -= 7;
            if(c2 > 9) c2 -= 7;

            values[i / 2] = (byte)(((c1 << 4) | c2) & 0xFF);
        }
        
        return values;
    }
    
    public static void threadSleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
    }
    
    
    public static StringBuilder ipToString(StringBuilder sb, long ip){
        sb.append(nums256[(int)(ip >> 24) & 0xFF]);
        sb.append(dot);
        sb.append(nums256[(int)(ip >> 16) & 0xFF]);
        sb.append(dot);
        sb.append(nums256[(int)(ip >> 8 ) & 0xFF]);
        sb.append(dot);
        sb.append(nums256[(int)(ip      ) & 0xFF]);
        return sb;
    }
    
    public static int bytesToInt(byte[] bytes, boolean netOrder){
        if(bytes == null){
            return 0;
        }
        
        return (netOrder?( 
                    (bytes[0] & 0xFF << 24) |
                    (bytes[1] & 0xFF << 16) |
                    (bytes[2] & 0xFF << 8 ) |
                    (bytes[3] & 0xFF      ) 
                ) : (
                    (bytes[3] & 0xFF << 24) |
                    (bytes[2] & 0xFF << 16) |
                    (bytes[1] & 0xFF << 8 ) |
                    (bytes[0] & 0xFF      ) 
                ) );
    }
    
    public static short bytesToShort(byte[] bytes, boolean netOrder){
        if(bytes == null){
            return 0;
        }
        
        return (netOrder?(short)( 
                    (bytes[0] & 0xFF << 8 ) |
                    (bytes[1] & 0xFF      ) 
                ) : (short)(
                    (bytes[1] & 0xFF << 8 ) |
                    (bytes[0] & 0xFF      ) 
                ) );
    }
    
    public static String catFile(URI file) throws FileNotFoundException, IOException{
        InputStream fin = file.toURL().openStream();
        byte[] data = new byte[fin.available()];
        fin.read(data);
        fin.close();
        
        return new String(data);
    }
}