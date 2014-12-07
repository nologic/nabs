/*
 * Settings.java
 *
 * Created on June 7, 2005, 2:59 PM
 */

package eunomia.config;

import java.util.*;
import java.awt.*;

import eunomia.core.data.*;
import java.io.IOException;
import eunomia.core.data.flow.Flow;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class Settings {
    private static final Settings ins;
    
    private Config config;
     
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
    }
    
    public static void save() throws IOException {
        String[] arr = new String[Flow.NUM_TYPES];
        for(int i = 0; i < Flow.NUM_TYPES; i++){
            arr[i] = "" + getTypeColor(i).getRGB();
        }
        ins.config.setArray("color", arr);
        ins.config.save();
    }
    
    public static Color getTypeColor(int type){
        return getTypeColor(Flow.typeNames[type]);
    }
    
    public static Color getTypeColor(String type){
        return (Color)typeToColor.get(type);
    }
    
    public static void setTypeColor(String type, Color c){
        typeToColor.put(type, c);
    }
    
    public static void setTypeColor(int type, Color c){
        setTypeColor(Flow.typeNames[type], c);
    }

    public static void addConfigChangeListener(ConfigChangeListener l){
        ins.config.addConfigChangeListener(l);
    }
    
    public static void removeConfigChangeListener(ConfigChangeListener l){
        ins.config.removeConfigChangeListener(l);
    }
}