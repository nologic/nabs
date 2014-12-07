package eunomia.plugin.gui.atas;

import eunomia.plugin.com.atas.HostInfo;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Intersection3 extends Role {
    private Role role1;
    private Role role2;
    private Role role3;
    
    private Map unique1;
    private Map unique2;
    private Map unique3;
    private Map common23;
    private Map common13;
    private Map common12;
    
    private int xDiff2 = 50;
    private int xDiff3;
    private int yDiff3 = 60;
    
    private Intersection intersection;
    
    public static Color defaultRoleColor = new Color(150,150,50);
    
    public Intersection3(Intersection i, Role r3) {
        super(Intersection3.makeName(i.getRole1().getRoleName(),
                i.getRole2().getRoleName(),
                r3.getRoleName()), 0, 0);
        role1 = i.getRole1();
        role2 = i.getRole2();
        role3 = r3;
        
        unique1 = new HashMap();
        unique2 = new HashMap();
        unique3 = new HashMap();
        common12 = new HashMap();
        common13 = new HashMap();
        common23 = new HashMap();
        
        int minWidth = role1.getWidth();
        if (role2.getWidth() < minWidth)
            minWidth = role2.getWidth();
        
        if (role3.getWidth() < minWidth)
            minWidth = role3.getWidth();
        
        role1.setWidth(minWidth);
        role1.setHeight(minWidth);
        role2.setWidth(minWidth);
        role2.setHeight(minWidth);
        role3.setWidth(minWidth);
        role3.setHeight(minWidth);
        
        adjustSizes(minWidth);
        
        setDraggable(false);
        role1.setDraggable(false);
        role2.setDraggable(false);
        role3.setDraggable(false);
        
        setX(role1.getX());
        setY(role1.getY());
        roleColor = new Color(21, 67, 153);
        
        intersection = i;
    }
    
    void setResizeMode(int x, int y) {
        //x -= xPos;
        //y -= yPos;
        //if(isRoleAt(x,y)) resizeState = false;
        int radius = (int)role1.getRadius();
        
        if(x - role1.getX() > radius + radius/8 && x - role2.getX() < radius + radius/8 && y < role3.getY()) {
            resizeState = false;
        } else if(y - role1.getY() > radius + radius/8 && y - role3.getY() < radius + radius/8) {
            resizeState = false;
        } else {
            resizeState = Math.abs(Intersection.getRadius(x-role1.getX(), y-role1.getY(), role1.getWidth()) -
                    role1.getRadius()) < 10 ||
                    Math.abs(Intersection.getRadius(x-role2.getX(), y-role2.getY(), role2.getWidth()) -
                    role2.getRadius()) < 10 ||
                    Math.abs(Intersection.getRadius(x-role3.getX(), y-role3.getY(), role3.getWidth()) -
                    role3.getRadius()) < 10;
        }
    }
    
    Cursor getResizeCursor(int x, int y) {
        if(!resizeState) return (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        x -= xPos;
        y -= yPos;
        double horBlock = role1.getWidth()/8;
        double verBlock = role1.getHeight()/2;
        
        if(x < horBlock || x > getWidth() - horBlock || y > role3.getY() + 7*verBlock/4)
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
    
    
    
    boolean isResizable() {return resizeState;}
    
    void resize(int x, int y) {
        //setResizeMode(x, y);
        //if(!resizeState) return;
        
        Role r = x > role2.getX() ? role2 : role1;
        if(y > role3.getY()) r = role3;
        
        x -= r.getX();
        y -= r.getY();
        int radiusDiff = (int)(Intersection.getRadius(x, y, r.getWidth()) - r.getRadius());
        
        int minSize = SIZE/2;
        if(radiusDiff < minSize - SIZE) return;
        int newX = getX() - radiusDiff/2;
        int newY = getY() - radiusDiff/2;
        
        if(newX >0 && newY > 0) {
            setX(newX);
            setY(newY);
            role1.setWidth(role1.getWidth() + radiusDiff);
            role2.setWidth(role2.getWidth() + radiusDiff);
            role3.setWidth(role3.getWidth() + radiusDiff);
            role1.setHeight(role1.getHeight() + radiusDiff);
            role2.setHeight(role2.getHeight() + radiusDiff);
            role3.setHeight(role3.getHeight() + radiusDiff);
        }
        
        adjustSizes(role1.getWidth());
    }
    
    private void adjustSizes(int w) {
        xDiff2 = w/2;
        xDiff3 = (w - xDiff2)/2;
        yDiff3 = w/2 + w/10;
        
        setWidth(w*2 - xDiff2);
        setHeight(w*2 - yDiff3);
    }
    
    private void setCommonHosts(Role r1, Role r2, Role r3, Map c1, Map c2, Map unique) {
        ArrayList<Host> hosts = r1.getHosts();
        
        for(int i = 0; i < hosts.size(); i++) {
            Host h = hosts.get(i);
            ///*
            if(r2.hostExists(h) &&  r3.hostExists(h)) {
                if(!hostExists(h)) super.insertHost(h.getHostInfo());
            }
            
            else if(r2.hostExists(h) && !r3.hostExists(h)) {
                if(!c1.containsValue(h))  c1.put(h.getHostInfo().getIp(), new Host(h.getHostInfo()));
            }
            
            else if(!r2.hostExists(h) && r3.hostExists(h)) {
                if(!c2.containsValue(h))
                    c2.put(h.getHostInfo().getIp(), new Host(h.getHostInfo()));
            }
            
            else
                unique.put(h.getHostInfo().getIp(), new Host(h.getHostInfo()));
        }
    }
    
    public ArrayList<String> getRoleNames() {
        ArrayList<String> array = new ArrayList<String>(3);
        array.add(role1.getRoleName());
        array.add(role2.getRoleName());
        array.add(role3.getRoleName());
        
        return (array);
    }
    
    private void setCommonHosts() {
        super.removeAllHosts();
        unique1.clear();
        unique2.clear();
        unique3.clear();
        common12.clear();
        common13.clear();
        common23.clear();
        setCommonHosts(role1, role2, role3, common12, common13, unique1);
        setCommonHosts(role2, role1, role3, common12, common23, unique2);
        setCommonHosts(role3, role1, role2, common13, common23, unique3);
    }
    
    void draw(Graphics g) {
        if(!getVisible() || !getDisplayable()) return;
        
        Color orig = g.getColor();
        Graphics2D g2 = (Graphics2D)g;
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        
        g.setColor(role1.getRoleColor());
        g.fillOval(role1.getX(), role1.getY(), role1.getWidth(), role1.getHeight());
        
        
        g.setColor(role2.getRoleColor());
        g.fillOval(role2.getX(), role2.getY(), role1.getWidth(), role2.getHeight());
        
        
        g.setColor(role3.getRoleColor());
        g.fillOval(role3.getX(), role3.getY(), role3.getWidth(), role3.getHeight());
        
        g.setColor(role1.getRoleColor());
        g.drawOval(role1.getX(), role1.getY(), role1.getWidth(), role1.getHeight());
        
        g.setColor(role2.getRoleColor());
        g.drawOval(role2.getX(), role2.getY(), role1.getWidth(), role2.getHeight());
        
        g.setColor(role3.getRoleColor());
        g.drawOval(role3.getX(), role3.getY(), role3.getWidth(), role3.getHeight());
        
        drawHosts(g);
        g2.setComposite(old);
        g.setColor(orig);
    }
    
    void drawHosts(Graphics g) {
        setCommonHosts();
        setHostPositions(unique1, role1.getX(), role1.getY(), true, false, false);
        setHostPositions(unique2, role2.getX(), role2.getY(), false, true, false);
        setHostPositions(unique3, role3.getX(), role3.getY(), false, false, true);
        setHostPositions(common12, role2.getX(), role2.getY(), true, true, false);
        setHostPositions(common13, role3.getX(), role3.getY(), true, false, true);
        setHostPositions(common23, role2.getX(), role2.getY(), false, true, true);
        setHostPositions(hosts, role2.getX(), role2.getY(), true, true, true);
        
        
        drawHostsMap(g, unique1);
        drawHostsMap(g, unique2);
        drawHostsMap(g, unique3);
        drawHostsMap(g, common12);
        drawHostsMap(g, common13);
        drawHostsMap(g, common23);
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
        return (role1.isRoleAt(x,y) || role2.isRoleAt(x,y) || role3.isRoleAt(x,y));
    }
    
    boolean isRoleAt(int x, int y, boolean role1Value, boolean role2Value, boolean role3Value) {
        return( (role1Value == role1.isRoleAt(x,y)) &&
                (role2Value == role2.isRoleAt(x,y)) &&
                (role3Value == role3.isRoleAt(x,y)));
    }
    
    private void setHostPositions(Map m, int xPos, int yPos, boolean r1, boolean r2, boolean r3) {
        if(m == null)
            return;
        
        Object[] objects = Role.getMapValuesArray(m);
        int size = Host.SIZE;
        int x = 3;
        int i = 0;
        int radius = role1.getWidth()/2;
        int y = radius - (int)Math.sqrt( radius*radius - (x-radius)*(x-radius));
        
        for(; i<objects.length; i++) {
            while(!isRoleAt(x+size+xPos, y+yPos, r1, r2, r3) && y < radius*2) {
                y += size;
            }
            
            if(!isRoleAt(x+xPos, y+size+yPos, r1, r2, r3) ||
                    !isRoleAt(x+xPos+size, y+size+yPos, r1, r2, r3)) {
                x += size;
                if(x > radius*2) break;
                
                y = radius - (int)Math.sqrt( radius*radius - (x-radius)*(x-radius));
                i--;
                continue;
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
        
        for(int j = i; j<objects.length; j++) {
            if(objects[j] instanceof Host) {
                ((Host)objects[j]).setVisible(false);
            }
        }
    }
    
    Host getHostAt(int x, int y) {
        Host h;
        if( (h = super.getHostAt(x, y, unique1)) != null)
            return h;
        
        if( (h = super.getHostAt(x, y, unique2)) != null)
            return h;
        
        if( (h = super.getHostAt(x, y, unique3)) != null)
            return h;
        
        if( (h = super.getHostAt(x, y, common12)) != null)
            return h;
        
        if( (h = super.getHostAt(x, y, common13)) != null)
            return h;
        
        if( (h = super.getHostAt(x, y, common23)) != null)
            return h;
        
        return super.getHostAt(x, y, hosts);
    }
    
    boolean isIntersect(Role role) {
        return(false);
    }
    
    boolean existsRole(Role r) {
        if(r == null)
            return false;
        
        return r.equalsRole(role1) || r.equalsRole(role2) || r.equalsRole(role3);
    }
    
    boolean equalsRole(Role r) {
        if(r == null)
            return false;
        
        if(!(r instanceof Intersection3))
            return false;
        
        Intersection3 i = (Intersection3)r;
        
        return ((role1.equalsRole(i.getRole1()) || role1.equalsRole(i.getRole2()) || role1.equalsRole(i.getRole3())) &&
                (role2.equalsRole(i.getRole1()) || role2.equalsRole(i.getRole2()) || role2.equalsRole(i.getRole3())) &&
                (role3.equalsRole(i.getRole1()) || role3.equalsRole(i.getRole2()) || role3.equalsRole(i.getRole3())) );
    }
    
    boolean isAtUnique(int x, int y) {
        int is1 = role1.isRoleAt(x,y) ? 1:0;
        int is2 = role2.isRoleAt(x,y) ? 1:0;
        int is3 = role3.isRoleAt(x,y) ? 1:0;
        return (is1 + is2 + is3 == 1);
    }
    
    ArrayList<HostInfo> getHostInfos() {
        ArrayList ret = super.getHostInfos();
        ret.addAll(Role.getHostInfosArray(unique1));
        ret.addAll(Role.getHostInfosArray(unique2));
        ret.addAll(Role.getHostInfosArray(unique3));
        ret.addAll(Role.getHostInfosArray(common12));
        ret.addAll(Role.getHostInfosArray(common13));
        ret.addAll(Role.getHostInfosArray(common23));
        
        return(ret);
    }
    
    ArrayList<HostInfo> getHostsAt(int x, int y) {
        boolean is1 = role1.isRoleAt(x,y);
        boolean is2 = role2.isRoleAt(x,y);
        boolean is3 = role3.isRoleAt(x,y);
        
        if(is1 && is2 && is3) return (super.getHostInfos());
        else if(is1 && is2) return (Role.getHostInfosArray(common12));
        else if(is1 && is3) return (Role.getHostInfosArray(common13));
        else if(is2 && is3) return (Role.getHostInfosArray(common23));
        else if(is1) return (Role.getHostInfosArray(unique1));
        else if(is2) return (Role.getHostInfosArray(unique2));
        else return (Role.getHostInfosArray(unique3));
    }
    
    String getNewRoleNameAt(int x, int y) {
        boolean is1 = role1.isRoleAt(x,y);
        boolean is2 = role2.isRoleAt(x,y);
        boolean is3 = role3.isRoleAt(x,y);
        
        if(is1 && is2 && is3)
            return ("Common of " + role1.getRoleName() + " & " + role2.getRoleName() + " & " + role3.getRoleName());
        else if(is1 && is2)
            return("In"  + role1.getRoleName() + " & " + role2.getRoleName() + ". Not in " + role3.getRoleName());
        else if(is1 && is3)
            return("In"  + role1.getRoleName() + " & " + role3.getRoleName() + ". Not in " + role2.getRoleName());
        else if(is2 && is3)
            return("In"  + role2.getRoleName() + " & " + role3.getRoleName() + ". Not in " + role1.getRoleName());
        else if(is1)
            return("In"  + role1.getRoleName() + ". Not in " + role2.getRoleName() + " & " + role3.getRoleName());
        else if(is2)
            return("In"  + role2.getRoleName() + ". Not in " + role1.getRoleName() + " & " + role3.getRoleName());
        else
            return("In"  + role3.getRoleName() + ". Not in " + role1.getRoleName() + " & " + role2.getRoleName());
    }
    
    static String makeName(String r1, String r2, String r3) {
        return("Intersection of " + r1+ " & " + r2 + " & " + r3);
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
        if(hostList == null) return;
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            for(int i = 0; i<hostList.size(); i++)
                region.remove(hostList.get(i).getIp());
        } else System.out.println("Invalid removeHost to Intersection3");
    }
    
    public void removeHost(ArrayList<String> roleNames, HostInfo hostInfo) {
        if(hostInfo == null) return;
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            region.remove(hostInfo.getIp());
        } else System.out.println("Invalid removeHost to Intersection3");
    }
    
    public void insertHost(ArrayList<String> roleNames, HostInfo hostInfo) {
        if(hostInfo == null) return;
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            region.put(hostInfo.getIp(), hostInfo);
        } else System.out.println("Invalid insertHost to Intersection3");
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
        if(hostList == null) return;
        Map region;
        if( (region = getRegionByName(roleNames)) != null) {
            for(int i = 0; i<hostList.size(); i++)
                region.put(hostList.get(i).getIp(), hostList.get(i));
        } else System.out.println("Invalid removeHost to Intersection3");
    }
    
    private Map getRegionByName(ArrayList<String> roleNames) {
        if(roleNames.size() == 1)
            return getRegionByName(roleNames.get(0));
        else if(roleNames.size() == 2)
            return getRegionByName(roleNames.get(0), roleNames.get(1));
        else if(roleNames.size() == 3)
            return getRegionByName(roleNames.get(0), roleNames.get(1), roleNames.get(2));
        return null;
    }
    
    private Map getRegionByName(String roleName) {
        if(role1.getRoleName().equals(roleName))
            return(unique1);
        else if(role2.getRoleName().equals(roleName))
            return(unique2);
        else if(role3.getRoleName().equals(roleName))
            return(unique3);
        else return (null);
    }
    
    private Map getRegionByName(String rn1, String rn2) {
        if( (role1.getRoleName().equals(rn1) &&
                role2.getRoleName().equals(rn2)) ||
                (role2.getRoleName().equals(rn1) &&
                role1.getRoleName().equals(rn2))) return(common12);
        
        if( (role1.getRoleName().equals(rn1) &&
                role3.getRoleName().equals(rn2)) ||
                (role3.getRoleName().equals(rn1) &&
                role1.getRoleName().equals(rn2))) return(common13);
        
        if( (role2.getRoleName().equals(rn1) &&
                role3.getRoleName().equals(rn2)) ||
                (role3.getRoleName().equals(rn1) &&
                role2.getRoleName().equals(rn2))) return(common23);
        
        return (null);
    }
    
    private Map getRegionByName(String rn1, String rn2, String rn3) {
        if( !role1.getRoleName().equals(rn1) &&
                !role2.getRoleName().equals(rn1) &&
                !role3.getRoleName().equals(rn1)) return(null);
        if( !role1.getRoleName().equals(rn2) &&
                !role2.getRoleName().equals(rn2) &&
                !role3.getRoleName().equals(rn2)) return(null);
        if( !role1.getRoleName().equals(rn3) &&
                !role2.getRoleName().equals(rn3) &&
                !role3.getRoleName().equals(rn3)) return(null);
        else return (super.hosts);
    }
    
    
    
    
    
    
    
    
    
    
    
    Role getRoleAt(int x, int y) {
        if(role1.isRoleAt(x,y)) return (role1);
        else if(role2.isRoleAt(x,y)) return(role2);
        else if(role3.isRoleAt(x,y)) return(role3);
        else return (null);
    }
    
    void setX(int x) {
        xPos = x;
        role1.setX(x);
        role2.setX(x + xDiff2);
        role3.setX(x + xDiff3);
    }
    
    void setY(int y) {
        yPos = y;
        role1.setY(y);
        role2.setY(y);
        role3.setY(y + yDiff3);
    }
    
    Role getRole1() {return role1;}
    Role getRole2() {return role2;}
    Role getRole3() {return role3;}
    Intersection getIntersection() {return intersection;}
    ArrayList<HostInfo> getUnique1() {return Role.getHostInfosArray(unique1);}
    ArrayList<HostInfo> getUnique2() {return Role.getHostInfosArray(unique2);}
    ArrayList<HostInfo> getUnique3() {return Role.getHostInfosArray(unique3);}
    ArrayList<HostInfo> getCommon12() {return Role.getHostInfosArray(common12);}
    ArrayList<HostInfo> getCommon13() {return Role.getHostInfosArray(common13);}
    ArrayList<HostInfo> getCommon23() {return Role.getHostInfosArray(common23);}
}
