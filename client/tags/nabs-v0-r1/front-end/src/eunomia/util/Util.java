/*
 * Util.java
 *
 * Created on June 22, 2005, 6:13 PM
 *
 */

package eunomia.util;

import java.net.InetAddress;
import java.security.*;
import java.util.Calendar;

/**
 *
 * @author Mikhail Sosonkin
 */

public class Util {
    private static String[] sizes = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
    private static String[] rates = {"B/s", "KB/s", "MB/s", "GB/s", "TB/s", "PB/s", "EB/s"};
    
    public static String convertBytes(long bytes){
        int count = 0;
        
        while(bytes > 1024.0){
            bytes >>= 10;
            ++count;
        }
        
        return new StringBuilder().append(Long.toString(bytes)).append(sizes[count]).toString();
    }
    
    public static StringBuilder convertBytes(StringBuilder append, double bytes, boolean truncate){
        int count = 0;
        double target = bytes;
        
        while(target > 1024.0){
            target /= 1024.0;;
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
}