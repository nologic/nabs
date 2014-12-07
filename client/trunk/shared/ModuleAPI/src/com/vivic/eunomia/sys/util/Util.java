/*
 * Util.java
 *
 * Created on June 22, 2005, 6:13 PM
 *
 */

package com.vivic.eunomia.sys.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.*;
import java.text.ParseException;
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
    private static long[] byte_scale = {1L, 1024L, 1024L*1024L, 1024L*1024L*1024L, 1024L*1024L*1024L*1024L, 1024L*1024L*1024L*1024L*1024L, 1024L*1024L*1024L*1024L*1024L*1024L};
    private static int SCALE_B = 0, SCALE_KB = 1, SCALE_MB = 2, SCALE_GB = 3, SCALE_TB = 4, SCALE_PB = 5, SCALE_EB = 6;
    
    static {
        dot = ".".toCharArray();
        nums256 = new char[256][];
        for (int i = 0; i < nums256.length; i++) {
            String str = (i + "");
            nums256[i] = str.toCharArray();
        }
    }
    
    public static long parseBytes(String bytes) throws ParseException {
        int scaleIndex = -1;
        String userRate = bytes.toLowerCase();

        if(userRate.indexOf("eb") != -1) {
            scaleIndex = SCALE_EB;
        } else if(userRate.indexOf("pb") != -1) {
            scaleIndex = SCALE_PB;
        } else if(userRate.indexOf("tb") != -1) {
            scaleIndex = SCALE_TB;
        } else if(userRate.indexOf("gb") != -1) {
            scaleIndex = SCALE_GB;
        } else if(userRate.indexOf("mb") != -1) {
            scaleIndex = SCALE_MB;
        } else if(userRate.indexOf("kb") != -1) {
            scaleIndex = SCALE_KB;
        } else if(userRate.indexOf("b") != -1) {
            scaleIndex = SCALE_B;
        } else {
            throw new ParseException("Unable to find scale.", 0);
        }

        char[] chars = userRate.toCharArray();
        StringBuilder digits = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if(Character.isDigit(c) || c == '.') {
                digits.append(c);
            }
        }

        double parsedRate = 0;
        try {
            parsedRate = Double.parseDouble(digits.toString());
        } catch(NumberFormatException ex) {
            throw new ParseException("Unable to parse number.", 0);
        }
        
        long actualRate = (long)(parsedRate * byte_scale[scaleIndex]);

        return actualRate;
    }
    
    public static String convertBytes(long bytes){
        int count = 0;
        
        while(bytes > 1024.0){
            bytes >>= 10;
            ++count;
        }
        
        return new StringBuilder().append(Long.toString(bytes)).append(sizes[count]).toString();
    }
    
    public static String md5(String str) throws NoSuchAlgorithmException {
        return hexEncode(md5(str.getBytes()));
    }
    
    public static byte[] md5(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return md5.digest(bytes);
    }
    
    public static byte[] md5ex(byte[] bytes) {
        try {
            return md5(bytes);
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }
    
    public static byte[] md5(URI uri) throws NoSuchAlgorithmException, MalformedURLException, IOException{
        byte[] bytes = new byte[8192];
        int read = 0;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        
        InputStream in = uri.toURL().openStream();
        while( (read = in.read(bytes)) != -1) {
            md5.update(bytes, 0, read);
        }
        in.close();
        
        return md5.digest();
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
    
    private static SecureRandom rand = new SecureRandom();
    public static int getRandomIntEx(String seed) {
        try {
            return getRandomInt(seed);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public static int getRandomInt(String seed) throws Exception {
        SecureRandom r = rand;
        if(seed != null){
            r = new SecureRandom(seed.getBytes());
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
    
    public static long getLongIp(byte[] buff) {
        long ip;
        long workLong1, workLong2, workLong3, workLong4;

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
    
    public static long getLongIp(InetAddress host){
        return getLongIp(host.getAddress());
    }
    
    public static long getLongIp(String ip) {
        try {
            return getLongIp(InetAddress.getByName(ip));
        } catch (UnknownHostException ex) {
            return 0;
        }
    }
    
    private static Calendar cal = Calendar.getInstance();
    public static String getTimeStamp(long timeMilis){
        return getTimeStamp(timeMilis, true, true);
    }
    
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
                f = cal.get(Calendar.MONTH) + 1;
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
    
    public static String ipToString(long ip) {
        return ipToString(new StringBuilder(), ip).toString();
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
    
    public static StringBuilder ipToString(StringBuilder sb, int[] ip){
        sb.append(nums256[ip[0] & 0xFF]);
        sb.append(dot);
        sb.append(nums256[ip[1] & 0xFF]);
        sb.append(dot);
        sb.append(nums256[ip[2] & 0xFF]);
        sb.append(dot);
        sb.append(nums256[ip[3] & 0xFF]);
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
    
    public static byte[] catFile(URI file) throws FileNotFoundException, IOException{
        InputStream fin = file.toURL().openStream();
        byte[] data = new byte[fin.available()];
        fin.read(data);
        fin.close();
        
        return data;
    }
    
    public static boolean deleteDir(File dir) {
        // Source: http://joust.kano.net/weblog/archives/000071.html
        
        // to see if this directory is actually a symbolic link to a directory,
        // we want to get its canonical path - that is, we follow the link to
        // the file it's actually linked to
        File candir;
        try {
            candir = dir.getCanonicalFile();
        } catch (IOException e) {
            return false;
        }
  
        // a symbolic link has a different canonical path than its actual path,
        // unless it's a link to itself
        if (!candir.equals(dir.getAbsoluteFile())) {
            // this file is a symbolic link, and there's no reason for us to
            // follow it, because then we might be deleting something outside of
            // the directory we were told to delete
            return false;
        }
  
        // now we go through all of the files and subdirectories in the
        // directory and delete them one by one
        File[] files = candir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
  
                // in case this directory is actually a symbolic link, or it's
                // empty, we want to try to delete the link before we try
                // anything
                boolean deleted = file.delete();
                if (!deleted) {
                    // deleting the file failed, so maybe it's a non-empty
                    // directory
                    if (file.isDirectory()) 
                        deleteDir(file);
                    // otherwise, there's nothing else we can do
                }
            }
        }
  
        // now that we tried to clear the directory out, we can try to delete it
        // again
        return dir.delete();  
    }
    
    public static Object[] arrayAppend(Object[] oldArr, Object app, int newSize) {
        Object[] tmp = (Object[])Array.newInstance(app.getClass(), newSize);
        System.arraycopy(oldArr, 0, tmp, 0, oldArr.length);
        tmp[oldArr.length] = app;
        
        return tmp;
    }
    
    public static Object[] arrayAppend(Object[] oldArr, Object app) {
        return arrayAppend(oldArr, app, oldArr.length + 1);
    }
    
    private static long ttime;
    public static long time() {
        if(ttime == 0) {
            ttime = System.currentTimeMillis();
            return 0;
        } else {
            long tmp = System.currentTimeMillis() - ttime;
            
            ttime = 0;
            
            return tmp;
        }
    }
}