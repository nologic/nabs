/*
 * Host.java
 *
 * Created on February 23, 2007, 2:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package eunomia.plugin.gui.atas;

import eunomia.plugin.com.atas.HostInfo;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author SDR30011
 */
public class Host {
    
    void draw(Graphics g) {
        if(!isVisible) return;
        
        Color orig = g.getColor();
        Graphics2D g2 = (Graphics2D)g;
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.setColor(hostColor);
        g.fillOval(xPos, yPos, radius*2, radius*2);
        g2.setComposite(old);
        g.setColor(orig);
        
    }
    
    /** Creates a new instance of Host */
    public Host(HostInfo hi) {
        xPos = 0;
        yPos = 0;
        radius = SIZE/2;
        info = hi;
        hostColor = Host.defaultHostColor;
    }
    
    public boolean isHostAt(int x, int y) {
        return(Role.isIntersect(x, y, getX() + radius, getY() + radius, radius/2));
    }
    
    int getX() { return xPos; }
    int getY() { return yPos; }
    boolean getVisible() { return isVisible;}
    void setX(int x) {xPos = x;}
    void setY(int y) {yPos = y;}
    void setXY(int x, int y) {xPos = x; yPos = y;}
    void setVisible(boolean isV) {isVisible = isV;}
    void setRadius(int r) {radius = r;}
    
    @Deprecated
    String getString() {return info.toString();}
    
    public String toString() {
        return getString();
    }
    
    HostInfo getHostInfo() {
        return (info);
    }
    
    public boolean equals(Host h) {
        return info.equals(h.getHostInfo());
    }
    
    public boolean equals(Object o) {
        if(o instanceof Host){
            return equals((Host)o);
        } else {
            return false;
        }
    }
    
    int radius;
    int xPos;
    int yPos;
    boolean isVisible;
    HostInfo info;
    static int SIZE = 10;
    protected Color hostColor;
    static Color defaultHostColor = new Color(40,40,100);
    
}
