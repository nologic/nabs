package eunomia.plugin.gui.atas;

import eunomia.plugin.com.atas.HostInfo;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Intersection extends Role {
    private Role role1;
    private Role role2;
    private Map unique1;
    private Map unique2;
    private int intWidth = 50;
    
    public Intersection(Role r1, Role r2) {
        super(Intersection.makeName(r1.getRoleName(), r2.getRoleName()), 0, 0);
        // role1 = r1; //new Role(r1);
        //role2 = r2; //new Role(r2);
        role1 = r1;
        role2 = r2;
        unique1 = Collections.synchronizedMap(new HashMap());
        unique2 = Collections.synchronizedMap(new HashMap());
        
        
        if(role1.getWidth() < role2.getWidth()) {
            role2.setWidth(role1.getWidth());
            role2.setHeight(role1.getHeight());
        }
        
        if(role1.getWidth() > role2.getWidth()) {
            role1.setWidth(role2.getWidth());
            role1.setHeight(role2.getHeight());
        }
        
        intWidth = role1.getWidth()/2;
        setWidth((int)(intWidth * 3));
        setHeight(role1.getHeight());
        
        setDraggable(false);
        role1.setDraggable(false);
        role2.setDraggable(false);
        roleColor = new Color(100, 20, 20);
        
        setX(Math.min(role1.getX(), role2.getX()));
        setY(role2.getY());
    }
    
    Intersection(Intersection i) {
        super(i.getRoleName(), i.getX(), i.getY());
        
        Role role1 = new Role(i.getRole1());
        Role role2 = new Role(i.getRole2());
        
        ArrayList hsts = i.getUnique1();
        unique1 = new HashMap();
        for(int j = 0; j < hsts.size(); j++)
            unique1.put(((HostInfo)hsts.get(j)).getIp(), new HostInfo((HostInfo)hsts.get(j)));
        
        hsts = i.getUnique2();
        unique2 = new HashMap();
        for(int j = 0; j < hsts.size(); j++)
            unique2.put( ((HostInfo)hsts.get(j)).getIp(), new HostInfo((HostInfo)hsts.get(j)));
        
    }
    
    
    public void removeHost(HostInfo host) {
        ArrayList<String> array = new ArrayList(1);
        array.add(roleName);
        removeHost(array, host);
    }
    
    public void removeHosts(String roleName, ArrayList<HostInfo> hostList) {
        ArrayList<String> array = new ArrayList(1);
        array.add(roleName);
        removeHosts(array, hostList);
    }
    
    public void removeHosts(ArrayList<String> roleNames,  ArrayList<HostInfo> hostList) {
        if(hostList == null)
            return;
        
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            for(int i = 0; i<hostList.size(); i++) {
                region.remove(hostList.get(i).getIp());
            }
        } /*else {
            System.out.println("Invalid removeHost to Intersection");
        }*/
    }
    
    public void removeHost(ArrayList<String> roleNames, HostInfo hostInfo) {
        if(hostInfo == null)
            return;
        
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            region.remove(hostInfo.getIp());
        } /*else {
            System.out.println("Invalid removeHost to Intersection");
        }*/
    }
    
    public void insertHost(ArrayList<String> roleNames, HostInfo hostInfo) {
        if(hostInfo == null) return;
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            region.put(hostInfo.getIp(), hostInfo);
        } /*else {
            System.out.println("Invalid insertHost to Intersection");
        }*/
    }
    
    public void insertHost(String roleName, HostInfo hostInfo) {
        ArrayList<String> array = new ArrayList(1);
        array.add(roleName);
        insertHost(array, hostInfo);
    }
    
    public void insertHosts(String roleName, ArrayList<HostInfo> hostList) {
        ArrayList<String> array = new ArrayList(1);
        array.add(roleName);
        insertHosts(array, hostList);
    }
    
    public void insertHosts(ArrayList<String> roleNames,  ArrayList<HostInfo> hostList) {
        if(hostList == null) 
            return;
        
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            for(int i = 0; i<hostList.size(); i++) {
                region.put(hostList.get(i).getIp(), hostList.get(i));
            }
        } /*else { 
            System.out.println("Invalid removeHost to Intersection");
        }*/
    }
    
    private Map getRegionByName(ArrayList<String> roleNames) {
        if(roleNames.size() == 1)
            return getRegionByName(roleNames.get(0));
        
        if(roleNames.size() == 2)
            return getRegionByName(roleNames.get(0), roleNames.get(1));
        
        return null;
    }
    
    private Map getRegionByName(String roleName) {
        if(role1.getRoleName().equals(roleName))
            return unique1;
        
        if(role2.getRoleName().equals(roleName))
            return unique2;
        
        return null;
    }
    
    private Map getRegionByName(String rn1, String rn2) {
        if( !role1.getRoleName().equals(rn1) && !role2.getRoleName().equals(rn1))
            return null;
        
        if( !role1.getRoleName().equals(rn2) && !role2.getRoleName().equals(rn2))
            return(null);
        
        return super.hosts;
    }
    
    public ArrayList<String> getRoleNames() {
        ArrayList<String> array = new ArrayList<String>(2);
        array.add(role1.getRoleName());
        array.add(role2.getRoleName());
        return (array);
    }
    
    private void setCommonHosts(Role r1, Role r2, Map unique) {
        ArrayList<Host> hosts = r1.getHosts();
        
        for(int i = 0; i < hosts.size(); i++) {
            Host h = hosts.get(i);
            
            if(r2.hostExists(h)) {
                if(!hostExists(h)) super.insertHost(h.getHostInfo());
            } else {
                unique.put(h.getHostInfo().getIp(), new Host(h.getHostInfo()));
            }
        }
    }
    
    private void setCommonHosts() {
        super.removeAllHosts();
        unique1.clear();
        unique2.clear();
        setCommonHosts(role1, role2, unique1);
        setCommonHosts(role2, role1, unique2);
    }
    
    void draw(Graphics g) {
        if(!getVisible() || !getDisplayable()) return;
        
        Graphics2D g2 = (Graphics2D)g;
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        
        Color orig = g.getColor();
        g.setColor(role1.getRoleColor());
        g.fillOval(role1.getX(), role1.getY(), getHeight(), getHeight());
        
        g.setColor(role2.getRoleColor());
        g.fillOval(role2.getX(), role2.getY(), getHeight(), getHeight());
        
        g.setColor(role1.getRoleColor());
        g.drawOval(role1.getX(), role1.getY(), getHeight(), getHeight());
        
        g.setColor(role2.getRoleColor());
        g.drawOval(role2.getX(), role2.getY(), getHeight(), getHeight());
        
        drawHosts(g);
        g2.setComposite(old);
        g.setColor(orig);
    }
    
    void drawHosts(Graphics g) {
        setCommonHosts();
        setHostPositions(unique1, role1.getX(), role1.getY(),  true, false);
        setHostPositions(unique2, role2.getX(), role2.getY(), false, true);
        setHostPositions(hosts, role2.getX(), role2.getY(), true, true);
        //g.clearRect(0,0,this.width, this.height);
        
        drawHostsMap(g, unique1);
        drawHostsMap(g, unique2);
        drawHostsMap(g, hosts);
    }
    
    void drawHostsMap(Graphics g, Map m) {
        Object[] objects = Role.getMapValuesArray(m);
        
        for(int i = 0; i<objects.length; i++) {
            if(objects[i] instanceof Host) {
                Host h = (Host)objects[i];
                h.draw(g);
            }
        }
    }
    
    boolean isRoleAt(int x, int y) {
        return (role1.isRoleAt(x,y) || role2.isRoleAt(x,y));
    }
    
    boolean isRoleAt(int x, int y, boolean role1Value, boolean role2Value) {
        return( (role1Value == role1.isRoleAt(x,y)) &&
                (role2Value == role2.isRoleAt(x,y)));
    }
    
    private void setHostPositions(Map m, int xPos, int yPos, boolean r1, boolean r2) {
        if(m == null)
            return;
        
        Object[] objects = Role.getMapValuesArray(m);
        int size = Host.SIZE;
        int x = 3;
        int i = 0;
        int radius = role1.getWidth()/2;
        int y = radius - (int)Math.sqrt( radius*radius - (x-radius)*(x-radius));
        
        for(; i<objects.length; i++) {
            if(x > radius*2 || y > radius*2) break;
            
            if(!isRoleAt(x + size + xPos, y + yPos, r1, r2)) {
                y += size;
            }
            
            if( !(r1 && r2) && isRoleAt(x+xPos+size, y+yPos+size, true, true)) {
                while(isRoleAt(x+xPos+size, y+yPos+size, true, true)) {
                    y += size;//{System.out.println(y);y += size;}
                }
            }
            
            if(!isRoleAt(x+xPos, y+size+yPos, r1, r2) || !isRoleAt(x+xPos+size, y+size+yPos, r1, r2)) {
                x += size;
                y = radius - (int)Math.sqrt( radius*radius - (x-radius)*(x-radius));
                i--;
                continue;
            }
            
            
            if(//!isRoleAt(x+size+xPos, y+yPos) ||
                    !isRoleAt(x+xPos, y+size+yPos, r1, r2) ||
                    !isRoleAt(x+xPos+size, y+size+yPos, r1, r2)
                    ) {
                break;
            }
            
            //System.out.println("("+x+","+y+")");
            if(objects[i] instanceof Host) {
                Host h = (Host)objects[i];
                h.setXY(x + xPos, y + yPos);
                h.setRadius(size/2);
                h.setVisible(true);
            }
            y += size;
        }
        
        //  int c=0;
        for(int j = i; j<objects.length; j++) {//c++;
            if(objects[j] instanceof Host) {
                // quick fix
                ((Host)objects[j]).setVisible(false);
            }
        }
        
        //System.out.println("Visible = " + (m.size() - c));
        
    }
    
    Host getHostAt(int x, int y) {
        Host h;
        if( (h = super.getHostAt(x, y, unique1)) != null) return(h);
        if( (h = super.getHostAt(x, y, unique2)) != null) return(h);
        return (super.getHostAt(x, y, hosts) );
    }
    
    boolean isIntersect(Role role) {
        return(role1.isIntersect(role) || role2.isIntersect(role));
    }
    
    boolean existsRole(Role r) {
        if(r == null) return false;
        return (r.equalsRole(role1) || r.equalsRole(role2));
    }
    
    boolean equalsRole(Role r) {
        if(r == null) return false;
        if(!(r instanceof Intersection)) return false;
        Intersection i = (Intersection)r;
        
        //System.out.println("INT " + role1.getRoleName() + " " + role2.getRoleName());
        //System.out.println("    " + i.getRole1().getRoleName() + " " + i.getRole2().getRoleName());
        
        return (  (role1.equalsRole(i.getRole1()) || role1.equalsRole(i.getRole2())) &&
                (role2.equalsRole(i.getRole1()) || role2.equalsRole(i.getRole2())) );
    }
    
    void setX(int x) {
        xPos = x;
        role1.setX(x);
        role2.setX(x + intWidth);
    }
    
    void setY(int y) {
        yPos = y;
        role1.setY(y);
        role2.setY(y);
    }
    
    void setResizeMode(int x, int y) {
        //x -= xPos;
        y -= yPos;
        //if(isRoleAt(x,y)) resizeState = false;
        int radius = (int)role1.getRadius();
        if(x - role1.getX() > radius + radius/8 && x - role2.getX() < radius + radius/8)
            resizeState = false;
        
        else
            resizeState = Math.abs(getRadius(x-role1.getX(), y, role1.getWidth()) -
                    role1.getRadius()) < 10 ||
                    Math.abs(getRadius(x-role2.getX(), y, role2.getWidth()) -
                    role2.getRadius()) < 10;
    }
    
    Cursor getResizeCursor(int x, int y) {
        if(!resizeState) return (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        x -= xPos;
        y -= yPos;
        double horBlock = role1.getWidth()/8;
        double verBlock = role1.getHeight()/2;
        
        if(x < horBlock || x > getWidth() - horBlock)
            return (Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        else if(x < 3*horBlock) {
            if(y > verBlock) return (Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            else return (Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        } else if(x < 5*2*horBlock)  return (Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        else {
            if(y < verBlock) return (Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
            else return (Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        }
    }
    
    static double getRadius(int x, int y, int w) {
        return Math.sqrt((x-w/2) * (x-w/2) + (y-w/2) * (y-w/2));
    }
    
    boolean isResizable() {return resizeState;}
    
    void resize(int x, int y) {
        //setResizeMode(x, y);
        //if(!resizeState) return;
        Role r = x > role2.getX() ? role2 : role1;
        
        x -= r.getX();
        y -= r.getY();
        int radiusDiff = (int)(getRadius(x, y, r.getWidth()) - r.getRadius());
        
        int minSize = SIZE/2;
        if(radiusDiff < minSize - SIZE) return;
        int newX = getX() - radiusDiff/2;
        int newY = getY() - radiusDiff/2;
        
        if(newX >0 && newY > 0) {
            setX(newX);
            setY(newY);
            role1.setWidth(role1.getWidth() + radiusDiff);
            role2.setWidth(role2.getWidth() + radiusDiff);
            role1.setHeight(role1.getHeight() + radiusDiff);
            role2.setHeight(role2.getHeight() + radiusDiff);
        }
        
        intWidth = role1.getWidth()/2;
        setWidth((int)(intWidth * 3));
        setHeight(role1.getHeight());
    }
    
    boolean isAtUnique(int x, int y) {
        int is1 = role1.isRoleAt(x,y) ? 1:0;
        int is2 = role2.isRoleAt(x,y) ? 1:0;
        return (is1 + is2 == 1);
    }
    
    
    ArrayList<HostInfo> getHostInfos() {
        ArrayList ret = super.getHostInfos();
        ret.addAll(Role.getHostInfosArray(unique1));
        ret.addAll(Role.getHostInfosArray(unique2));
        
        return(ret);
    }
    
    ArrayList<HostInfo> getHostsAt(int x, int y) {
        boolean is1 = role1.isRoleAt(x,y);
        boolean is2 = role2.isRoleAt(x,y);
        
        if(is1 && is2) return (super.getHostInfos());
        else if(is1) return (Role.getHostInfosArray(unique1));
        else return (Role.getHostInfosArray(unique2));
    }
    
    String getNewRoleNameAt(int x, int y) {
        boolean is1 = role1.isRoleAt(x,y);
        boolean is2 = role2.isRoleAt(x,y);
        
        if(is1 && is2) return ("Common of " + role1.getRoleName() + " & " + role2.getRoleName());
        else if(is1)   return("In"  + role1.getRoleName() + ". Not in " + role2.getRoleName());
        else           return("In"  + role2.getRoleName() + ". Not in " + role1.getRoleName());
    }
    
    Role getRoleAt(int x, int y) {
        if(role1.isRoleAt(x,y)) return (role1);
        else if(role2.isRoleAt(x,y)) return(role2);
        else return (null);
    }
    
    boolean hasRole(String roleName) {
        return ( roleName.equals(role1.getRoleName()) ||
                roleName.equals(role2.getRoleName()) );
    }
    
    static String makeName(String r1, String r2) {
        return("Intersection of " + r1+ " & " + r2);
    }
    
    Role getRole1() {return role1;}
    Role getRole2() {return role2;}
    ArrayList<HostInfo> getUnique1() {return Role.getHostInfosArray(unique1);}
    ArrayList<HostInfo> getUnique2() {return Role.getHostInfosArray(unique2);}
}