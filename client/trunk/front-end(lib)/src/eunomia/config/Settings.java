/*
 * Settings.java
 *
 * Created on June 7, 2005, 2:59 PM
 */

package eunomia.config;

import com.vivic.eunomia.sys.frontend.GlobalSettings;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author  Mikhail Sosonkin
 */

public class Settings implements GlobalSettings {
    private static final Settings ins;
    
    // temporary
    public static final String[] typeNames = {"Plain-Text", "Image-BMP", 
        "Audio-WAV", "Compressed", "Image-JPG", "Audio-MP3", "Video-MPG", 
        "Encrypted"};
    
    private Config config;
    private int refreshInterval;
     
    private HashMap typeToColor;
    
    static {
        ins = new Settings();
    }

    private Settings() {
        typeToColor = new HashMap();
        typeToColor.put("Plain-Text", Color.BLACK);
        typeToColor.put("Image-BMP", Color.BLUE);
        typeToColor.put("Audio-WAV", Color.CYAN);
        typeToColor.put("Compressed", Color.GREEN);
        typeToColor.put("Image-JPG", Color.MAGENTA);
        typeToColor.put("Audio-MP3", Color.ORANGE);
        typeToColor.put("Video-MPG", Color.RED);
        typeToColor.put("Encrypted", Color.YELLOW);
        
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
    
    public static Settings v() {
        return ins;
    }
    
    public void reset(){
        setTypeColor("Plain-Text", Color.BLACK);
        setTypeColor("Image-BMP", Color.BLUE);
        setTypeColor("Audio-WAV", Color.CYAN);
        setTypeColor("Compressed", Color.GREEN);
        setTypeColor("Image-JPG", Color.MAGENTA);
        setTypeColor("Audio-MP3", Color.ORANGE);
        setTypeColor("Video-MPG", Color.RED);
        setTypeColor("Encrypted", Color.YELLOW);
        refreshInterval = 1500;
    }
    
    public void save() throws IOException {
        String[] arr = new String[8];
        for(int i = 0; i < arr.length; i++){
            arr[i] = "" + getTypeColor(i).getRGB();
        }
        config.setArray("color", arr);
        config.setInt("refreshInterval", ins.refreshInterval);
        config.save();
    }
    
    public Color getTypeColor(int type){
        return getTypeColor(typeNames[type]);
    }
    
    public Color getTypeColor(String type){
        return (Color)typeToColor.get(type);
    }
    
    public void setTypeColor(String type, Color c){
        typeToColor.put(type, c);
    }
    
    public void setTypeColor(int type, Color c){
        setTypeColor(typeNames[type], c);
    }

    public void addConfigChangeListener(ConfigChangeListener l){
        config.addConfigChangeListener(l);
    }
    
    public void removeConfigChangeListener(ConfigChangeListener l){
        config.removeConfigChangeListener(l);
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}