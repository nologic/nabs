/*
 * Settings.java
 *
 * Created on June 7, 2005, 2:59 PM
 */

package eunomia.config;

import java.util.*;
import java.awt.*;

import java.io.IOException;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class Settings {
    private static final Settings ins;
    
    // temporary
    public static final String[] typeNames = {"Plain-Text", "Image-BMP", 
        "Audio-WAV", "Compressed", "Image-JPG", "Audio-MP3", "Video-MPG", 
        "Encrypted"};
    
    private Config config;
    private int refreshInterval;
     
    private static HashMap typeToColor;
    
    static {
        typeToColor = new HashMap();
        typeToColor.put("Plain-Text", Color.BLACK);
        typeToColor.put("Image-BMP", Color.BLUE);
        typeToColor.put("Audio-WAV", Color.CYAN);
        typeToColor.put("Compressed", Color.GREEN);
        typeToColor.put("Image-JPG", Color.MAGENTA);
        typeToColor.put("Audio-MP3", Color.ORANGE);
        typeToColor.put("Video-MPG", Color.RED);
        typeToColor.put("Encrypted", Color.YELLOW);
        ins = new Settings();
    }

    private Settings() {
        config = Config.getConfiguration("global");
        Object[] arr = config.getArray("color", null);
        refreshInterval = config.getInt("refreshInterval", 1500);
        
        if(arr == null){
            try {
                save();
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            for(int i = 0; i < arr.length; i++){
                setTypeColor(i, Color.decode(arr[i].toString()));
            }
        }
    }
    
    public static void reset(){
        setTypeColor("Plain-Text", Color.BLACK);
        setTypeColor("Image-BMP", Color.BLUE);
        setTypeColor("Audio-WAV", Color.CYAN);
        setTypeColor("Compressed", Color.GREEN);
        setTypeColor("Image-JPG", Color.MAGENTA);
        setTypeColor("Audio-MP3", Color.ORANGE);
        setTypeColor("Video-MPG", Color.RED);
        setTypeColor("Encrypted", Color.YELLOW);
        ins.refreshInterval = 1500;
    }
    
    public static void save() throws IOException {
        String[] arr = new String[8];
        for(int i = 0; i < arr.length; i++){
            arr[i] = "" + getTypeColor(i).getRGB();
        }
        ins.config.setArray("color", arr);
        ins.config.setInt("refreshInterval", ins.refreshInterval);
        ins.config.save();
    }
    
    public static Color getTypeColor(int type){
        return getTypeColor(typeNames[type]);
    }
    
    public static Color getTypeColor(String type){
        return (Color)typeToColor.get(type);
    }
    
    public static void setTypeColor(String type, Color c){
        typeToColor.put(type, c);
    }
    
    public static void setTypeColor(int type, Color c){
        setTypeColor(typeNames[type], c);
    }

    public static void addConfigChangeListener(ConfigChangeListener l){
        ins.config.addConfigChangeListener(l);
    }
    
    public static void removeConfigChangeListener(ConfigChangeListener l){
        ins.config.removeConfigChangeListener(l);
    }

    public static int getRefreshInterval() {
        return ins.refreshInterval;
    }

    public static void setRefreshInterval(int refreshInterval) {
        ins.refreshInterval = refreshInterval;
    }
}