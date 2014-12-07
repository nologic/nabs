package eunomia.plugin.gui.atas;

import eunomia.plugin.com.atas.HostInfo;
import eunomia.util.Util;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Role {
    protected String roleName;
    protected Map hosts;
    protected int xPos;
    protected int yPos;
    protected int height;
    protected int width;
    protected boolean isVisible;
    protected boolean isDisplayable;
    protected boolean isDraggable;
    protected Color roleColor;
    protected boolean resizeState;
    
    public static int SIZE = 100;
    private Cursor cursor;
    private boolean isSelected;
    
    public Role(String rName, int x, int y) {
        hosts = Collections.synchronizedMap(new HashMap());
        roleName = new String(rName);
        xPos = x;
        yPos = y;
        height = SIZE;
        width = SIZE;
        isVisible = true;
        isDisplayable = true;
        isDraggable = true;
        //roleColor = new Color(generator.nextInt(256), generator.nextInt(256), generator.nextInt(256));
        // to get the darker colors but not too dark
        roleColor = new Color((Util.getRandomIntEx(null) & 0x7F + 20), (Util.getRandomIntEx(null) & 0x7F + 40), (Util.getRandomIntEx(null) & 0x7F + 20));
    }
    
    public Role(Role r) {
        roleName = new String(r.getRoleName());
        xPos = r.getX();
        yPos = r.getY();
        height = r.getHeight();
        width = r.getWidth();
        isVisible = r.getVisible();
        isDraggable = r.getDraggable();
        isDisplayable = getDisplayable();
        
        hosts = Collections.synchronizedMap(new HashMap());
        ArrayList<HostInfo> list = r.getHostInfos();
        for(int i = 0; i < list.size(); i++)
            insertHost(new HostInfo(list.get(i)));
    }
    
    
    public void insertHost( HostInfo hostInfo) {
        if(hostInfo != null)
            hosts.put(hostInfo.getIp(), new Host(hostInfo));
    }
    
    
    public void insertHost(ArrayList<String> roleNames, HostInfo hostInfo) {
        if(roleNames.get(0).equals(getRoleName()))
            insertHost(hostInfo);
    }
    
    
    public void insertHosts(ArrayList<HostInfo> hostList) {
        if(hostList != null)
            for(int i=0; i<hostList.size(); i++) {
            insertHost((HostInfo)hostList.get(i));
            }
    }
    
    public void removeHost(HostInfo host) {
        if(host != null)
            hosts.remove(host.getIp());
    }
    
    public void removeHosts(ArrayList<HostInfo> hostList) {
        if(hostList != null)
            for(int i=0; i<hostList.size(); i++) {
            removeHost((HostInfo)hostList.get(i));
            }
    }
    
    public void insertHost(String roleName, HostInfo hostInfo) {
        if(roleName.equals(getRoleName())) insertHost(hostInfo);
    }
    
    public void insertHosts(String roleName,  ArrayList<HostInfo> hostList) {
        if(roleName.equals(getRoleName())) insertHosts(hostList);
    }
    
    public void insertHosts(ArrayList<String> roleNames,  ArrayList<HostInfo> hostList) {
        if(roleNames.get(0).equals(getRoleName())) insertHosts(hostList);
    }
    
    public void removeHost(String roleName, HostInfo hostInfo) {
        if(roleName.equals(getRoleName())) removeHost(hostInfo);
    }
    
    public void removeHosts(String roleName,  ArrayList<HostInfo> hostList) {
        if(roleName.equals(getRoleName())) removeHosts(hostList);
    }
    
    public void removeHosts(ArrayList<String> roleNames,  ArrayList<HostInfo> hostList) {
        if(roleNames.get(0).equals(getRoleName())) removeHosts(hostList);
    }
    
    
    void draw(Graphics g) {
        if(!getVisible() || !getDisplayable())
            return;
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color orig = g.getColor();
        g.setColor(roleColor);
        
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        
        g.fillOval(getX(), getY(), getHeight(), getHeight());
        if(isSelected) {
            g.setColor(Color.YELLOW);
            g.drawOval(getX(), getY(), getHeight()+1, getHeight()+1);
        }
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        
        g.setColor(Color.BLACK);
        g2.drawString(getRoleName(), getX(), getY() + (int)(getRadius() * 2) + 8);
        
        g2.setComposite(old);
        g.setColor(orig);
        
        drawHosts(g);
    }
    
    
    private void setHostPositions() {
        if(hosts == null) return;
        
        Object[] objects = Role.getMapValuesArray(hosts);
        int size = Host.SIZE;
        int x = 3;
        int i = 0;
        int radius = (int)getRadius();
        int y = radius - (int)Math.sqrt( radius*radius - (x-radius)*(x-radius));
        
        for(; i<objects.length; i++) {
            
            if(!isRoleAt(x+size+xPos, y+yPos)) {
                y += size;
            }
            
            else if(!isRoleAt(x+xPos, y+size+yPos) || !isRoleAt(x+xPos+size, y+size+yPos)) {
                x += size;
                y = radius - (int)Math.sqrt( radius*radius - (x-radius)*(x-radius));
                i--;
                continue;
            }
            
            if(//!isRoleAt(x+size+xPos, y+yPos) ||
                    !isRoleAt(x+xPos, y+size+yPos) ||
                    !isRoleAt(x+xPos+size, y+size+yPos)
                    ) break;
            
            if(objects[i] instanceof Host) {
                Host h = (Host)objects[i];
                h.setXY(x + xPos, y + yPos);
                h.setRadius(size/2);
                h.setVisible(true);
            }
            y += size;
        }
        
        //int c=0;
        for(int j = i; j<objects.length; j++) {//c++;
            if(objects[j] instanceof Host) {
                ((Host)objects[j]).setVisible(false);
            }
        }
        
        //System.out.println("Visible = " + (this.hosts.size() - c));
    }
    
    void drawHosts(Graphics g) {
        if(hosts == null) 
            return;
        setHostPositions();
        Object[] objects = Role.getMapValuesArray(hosts);
        
        for(int i = 0; i<objects.length; i++) {
            if(objects[i] instanceof Host) {
                Host h = (Host)objects[i];
                h.draw(g);
            }
        }
        
    }
    
    
    static boolean isIntersect(int x1, int y1, int x2, int y2, int radius) {
        return (   (x1-x2) * (x1-x2) +
                (y1-y2) * (y1-y2) <
                radius * radius * 4);
    }
    
    boolean isIntersect(Role role) {
        return(isIntersect(getX(), getY(), role.getX(), role.getY(), getWidth()/2));
    }
    
    boolean isIntersectRectangle(int x, int y, int w, int h) {
        return( ((getX() <= x     && x <= getX() + getWidth())  ||
                (getX() <= x + w && x <= getX() + getWidth())) &&
                ((getY() <= y     && y <= getY() + getHeight()) ||
                (getY() <= y + h && y <= getY() + getHeight())));
    }
    
    boolean isRoleAt(int x, int y) {
        return(isIntersect(x, y, getX() + width/2, getY() + height/2, height/4));
    }
    
    void removeAllHosts() {
        hosts.clear();
        //hosts = null;
    }
    
    public ArrayList<String> getRoleNames() {
        ArrayList<String> array = new ArrayList<String>(1);
        array.add(getRoleName());
        return (array);
    }
    
    int getX() { return xPos; }
    int getY() { return yPos; }
    int getWidth() { return width; }
    int getHeight() { return height; }
    String getRoleName() { return roleName; }
    //ArrayList<HostInfo> getHosts() { return new ArrayList(hosts.keySet().toArray()); }
    boolean getVisible() { return isVisible; }
    boolean getDisplayable() { return isDisplayable; }
    boolean getDraggable() { return isDraggable; }
    boolean hostExists(Host h) {return hosts.containsValue(h);}
    
    ArrayList<HostInfo> getHostInfos() {
        return(getHostInfosArray(hosts));
    }
    
    ArrayList<Host> getHosts() {
        return(getHostsArray(hosts));
    }
    
    static ArrayList<Host> getHostsArray(Map m) {
        ArrayList<Host> list = new ArrayList<Host>(m.size());
        Object[] objs = Role.getMapValuesArray(m);
        for(int i=0; i<objs.length; i++) {
            if(objs[i] instanceof Host){
                list.add((Host)objs[i]);
            }
        }
        return(list);
        
    }
    
    static ArrayList<HostInfo> getHostInfosArray(Map m) {
        ArrayList list = new ArrayList(m.size());
        Object[] objs = Role.getMapValuesArray(m);
        for(int i=0; i<objs.length; i++)
            if(objs[i] instanceof Host) {
                list.add(((Host)objs[i]).getHostInfo());
            }
        
        return(list);
        
    }
    
    public Color getRoleColor(){
        return roleColor;
    }
    
    void setX(int x) {xPos = x;}
    void setY(int y) {yPos = y;}
    void setHeight(int h) {height = h;}
    void setWidth(int w) {width = w;}
    void setDisplayable(boolean isD) {isDisplayable = isD;}
    void setDraggable(boolean isD) {isDraggable = isD;}
    void setVisible(boolean isV) {
        isVisible = isV;
        if(!isV && isDisplayable) {
            setX(0);
            setY(0);
        }
    }
    
    protected Host getHostAt(int x, int y, Map m) {
        if(m == null) return (null);
        Iterator<Object> itr = m.values().iterator();
        while(itr.hasNext()) {
            Object o = itr.next();
            if(o instanceof Host) {
                Host host = (Host)o;
                if(host.getVisible() && host.isHostAt(x,y)) {
                    return host;
                }
            }
        }
        return (null);
    }
    
    Host getHostAt(int x, int y) {
        return(getHostAt(x,y,hosts));
    }
    
    boolean existsRole(Role r) {
        return equalsRole(r);
    }
    
    
    boolean equalsRole(Role r) {
        if(r == null) 
            return false;

        return r.getRoleName().equals(roleName);
    }
    
    ArrayList<HostInfo> getHostsAt(int origX, int origY) {
        return getHostInfos();
    }
    
    String getNewRoleNameAt(int x, int y) {
        return this.getRoleName();
    }
    
    void setResizeMode(int x, int y) {
        x -= xPos;
        y -= yPos;
        resizeState = Math.abs(getRadius(x, y) - getRadius()) < 5;
    }
    
    Cursor getResizeCursor(int x, int y) {
        if(!resizeState) return (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        x -= xPos;
        y -= yPos;
        double horBlock = width/8;
        double verBlock = height/2;
        
        if(x < horBlock || x > 7*horBlock)
            return (Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        else if(x < 3*horBlock) {
            if(y > verBlock) return (Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            else return (Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        } else if(x < 5*horBlock)  return (Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        else //if(x < 7*horBlock)
        {
            if(y < verBlock) return (Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            else return (Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        }
    }
    
    double getRadius() {
        return width/2;
    }
    
    double getRadius(int x, int y) {
        return Math.sqrt((x-width/2) * (x-width/2) + (y-height/2) * (y-height/2));
    }
    
    boolean isResizable() {return resizeState;}
    
    void resize(int x, int y) {
        //setResizeMode(x, y);
        //if(!resizeState) return;
        x -= xPos;
        y -= yPos;
        int radiusDiff = (int)(getRadius(x, y) - getRadius());
        
        int minSize = 10;
        if(radiusDiff < minSize - SIZE) return;
        int newX = getX() - radiusDiff/2;
        int newY = getY() - radiusDiff/2;
        
        if(newX >0 && newY > 0) {
            setX(newX);
            setY(newY);
            width  += radiusDiff;
            height += radiusDiff;
        }
    }
    
    boolean isAtUnique(int x, int y) {
        return true;
    }
    
    Role getRoleAt(int x, int y) {
        if(isRoleAt(x,y)) return (this);
        else return (null);
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean b) {
        isSelected = b;
    }
    
    // hack to prevent IndexOutOfBounds exception.
    private static Object lock = new Object();
    public static Object[] getMapValuesArray(Map map) {
        boolean tAgain = true;
        while(tAgain) {
            try {
                return map.values().toArray();
            } catch (Exception e){
            }
        }
        
        return null;
    }
}
