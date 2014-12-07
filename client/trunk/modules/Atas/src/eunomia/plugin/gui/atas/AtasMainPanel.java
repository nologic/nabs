/*
 * AtasMainPanel.java
 *
 * Created on January 17, 2007, 12:04 PM
 */

package eunomia.plugin.gui.atas;

import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import eunomia.plugin.com.atas.HostInfo;
import eunomia.plugin.com.atas.RoleChangeListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JOptionPane;

/**
 *
 * @author  Radion Khait
 */
public class AtasMainPanel extends javax.swing.JPanel
        implements RoleChangeListener, ComponentListener {

    private Map roles;
    private Image imBuffer;
    //private int width;
    //private int height;
    private RoleChangeListener rcl;
    private boolean inDrag;
    private Role draggingRole;
    
    //Graphics context associated with image buffer.
    private Graphics imG;
    private Role resizeRole;
    
    /** Creates new form AtasMainPanel */
    public AtasMainPanel() {
        roles = new ConcurrentHashMap();
    }
    
    public void init(ConsoleModuleManager manager) {
        RoleDragger dragger = new RoleDragger(this);
        ClickManager clickManager = new ClickManager(this, manager);
        this.setComponentPopupMenu(clickManager.getPopupMenu());
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
        //addMouseListener(clickManager);
        addComponentListener(this);
    }
    
    public void setRoleChangeListener(RoleChangeListener rcl){
        this.rcl = rcl;
    }
    
    public RoleChangeListener getRoleChangeListener(){
        return this;
    }
    
    public void insertRole(String roleName, ArrayList<HostInfo> hostList) {
        if(roles.get(roleName) != null) 
            return;
        
        Point p = getEmptySpot(Role.SIZE, Role.SIZE);
        if(p == null) {
            showNoSpace("insertRole");
            return;
        }
        Role role = new Role(roleName, (int)p.getX(), (int)p.getY());
        role.insertHosts(hostList);
        roles.put(roleName, role);
        repaint();
    }
    
    public Role insertRole(String roleName, ArrayList<HostInfo> hostList, int x, int y) {
        if(roles.get(roleName) != null) 
            return null;
        
        Role role = new Role(roleName, x, y);
        role.insertHosts(hostList);
        roles.put(roleName, role);
        repaint();
        
        return role;
    }
    
    public void removeRole(String roleName) {
        Role role = (Role)(roles.get(roleName));
        if(role == null) 
            return;
        
        role.removeAllHosts();
        roles.remove(roleName);
        repaint();
    }
    
    public Object[] getRoles() {
        return Role.getMapValuesArray(roles);
    }
    
    public ArrayList<HostInfo> getHostsOfRole(String roleName) {
        Role role = (Role)(roles.get(roleName));
        if(role == null)
            return null;
        
        return role.getHostInfos();
    }
    
    public void insertHost(String roleName, HostInfo host) {
        ArrayList<HostInfo> array = new ArrayList(1);
        array.add(host);
        insertHosts(roleName, array);
    }
    
    public void insertHost(ArrayList<String> roleNames, HostInfo host) {
        ArrayList<HostInfo> array = new ArrayList(1);
        array.add(host);
        insertHosts(roleNames, array);
    }
    
    public void insertHosts(String roleName, ArrayList<HostInfo> hostList) {
        ArrayList<Role> rs = findRoles(roleName);
        
        if(rs == null || rs.size() == 0) {
            Point p = getEmptySpot(Role.SIZE, Role.SIZE);
            if(p == null) {
                showNoSpace("insertHost");
                return;
            }
            Role role = new Role(roleName, (int)p.getX(), (int)p.getY());
            roles.put(roleName, role);
            return;
        }
        
        for(int i = 0; i<rs.size(); i++) {
            rs.get(i).insertHosts(roleName, hostList);
        }
        repaint();
    }
    
    public void insertHosts(ArrayList<String> roleNames,  ArrayList<HostInfo> hostList) {
        if(roleNames == null || roleNames.size() == 0) {
            return;
        } else if(roleNames.size() == 1) {
            insertHosts(roleNames.get(0), hostList);
        } else if(roleNames.size() == 2) {
            ArrayList<Role> rs = findRoles(roleNames.get(0), roleNames.get(1));
            if(rs == null || rs.size() == 0){
                System.out.println("Invalid attempt to insert Host to " +
                        roleNames.get(0)+","+roleNames.get(1));
            } else {
                for(int i = 0; i<rs.size(); i++) {
                    rs.get(i).insertHosts(roleNames, hostList);
                }
            }
        }
        
        else if(roleNames.size() == 3) {
            Intersection3 i = findIntersection3(roleNames.get(0), roleNames.get(1), roleNames.get(2));
            if(i != null) {
                i.insertHosts(roleNames, hostList);
            } else {
                System.out.println("Invalid attempt to insert Host to " + roleNames.get(0)+
                        "," + roleNames.get(1) + "," + roleNames.get(2));
            }
        }
    }
    
    public void removeHost(String roleName, HostInfo host) {
        ArrayList<HostInfo> array = new ArrayList(1);
        array.add(host);
        removeHosts(roleName, array);
    }
    
    public void removeHost(ArrayList<String> roleNames, HostInfo host) {
        ArrayList<HostInfo> array = new ArrayList(1);
        array.add(host);
        removeHosts(roleNames, array);
    }
    
    public void removeHosts(String roleName, ArrayList<HostInfo> hostList) {
        ArrayList<Role> rs = findRoles(roleName);
        
        if(rs == null) {
            return;
        }
        
        for(int i = 0; i<rs.size(); i++) {
            rs.get(i).removeHosts(roleName, hostList);
        }
        repaint();
    }
    
    public void removeHosts(ArrayList<String> roleNames,  ArrayList<HostInfo> hostList) {
        if(roleNames == null || roleNames.size() == 0) {
            return;
        } else if(roleNames.size() == 1) {
            removeHosts(roleNames.get(0), hostList);
        } else if(roleNames.size() == 2) {
            ArrayList<Role> rs = findRoles(roleNames.get(0), roleNames.get(1));
            if(rs == null || rs.size() == 0) {
                System.out.println("Invalid attempt to remove Host to " +
                        roleNames.get(0)+","+roleNames.get(1));
            } else {
                for(int i = 0; i<rs.size(); i++) {
                    rs.get(i).removeHosts(roleNames, hostList);
                }
            }
        } else if(roleNames.size() == 3) {
            Intersection3 i = findIntersection3(roleNames.get(0), roleNames.get(1), roleNames.get(2));
            if(i != null) {
                i.removeHosts(roleNames, hostList);
            } else {
                System.out.println("Invalid attempt to remove Host to " + roleNames.get(0)+
                        "," + roleNames.get(1) + "," + roleNames.get(2));
            }
        }
    }
    
    public void removeIntersection(ArrayList<String> rolenames) {
        System.out.println("removeIntersection not implemented on the GUI");
    }
    
    boolean isEmptySpot(int x, int y, int w, int h) {
        Object [] obj = getRoles();
        for(int i = 0; i < obj.length; i++) {
            Role role = (Role)obj[i];
            if(role.getVisible() && role.isIntersectRectangle(x,y,w,h))
                return false;
        }
        return true;
    }
    
    Point getEmptySpot(int widthOfSpot, int heightOfSpot) {
        if(roles.size() == 0) {
            return new Point(0,0);
        }
        
        Object [] obj = getRoles();
        //int radius = ((Role)obj[0]).getSize()/2;
        boolean taken;
        int height = this.getHeight();
        int width = this.getWidth();
        for (int y = 0; y < height; y += 10) {
            for (int x = 0; x < width; x += 10) {
                taken = false;
                
                for(int i = 0; i < obj.length; i++) {
                    Role role = (Role)obj[i];
                    
                    if(role.getVisible() && role.isIntersectRectangle(x, y, widthOfSpot, heightOfSpot)) {
                        taken = true;
                        break;
                    }
                }
                
                if(!taken && x + widthOfSpot <= width && y + heightOfSpot <= height)
                    return (new Point(x,y));
            }
        }
        
        return(null);
    }
    
    public void setIntersection(ArrayList<String> roleNames) {
        if(roleNames.size() < 2 || roleNames.size() > 3) {
            System.out.println("Invalid number of set intersections: " + roleNames.size());
            return;
        }
        
        Role r1 = (Role)roles.get(roleNames.get(0));
        if(r1 == null) {
            System.out.println("Invalid role name: " + roleNames.get(0));
            return;
        }
        
        Role r2 = (Role)roles.get(roleNames.get(1));
        if(r2 == null) {
            System.out.println("Invalid role name: " + roleNames.get(1));
            return;
        }
        
        if(roleNames.size() == 2) {
            setIntersection(r1, r2);
            return;
        }
        
        Role r3 = (Role)roles.get(roleNames.get(2));
        if(r3 == null) {
            System.out.println("Invalid role name: " + roleNames.get(2));
            return;
        }
        
        String s1 = r1.getRoleName();
        String s2 = r2.getRoleName();
        String s3 = r3.getRoleName();
        
        Intersection i = null;
        if((i = findIntersection(s2, s3, s1)) != null) {
            if(!i.hasRole(s1))  setIntersection(r1, i);
            else if(!i.hasRole(s2))  setIntersection(r2, i);
            else if(!i.hasRole(s3))  setIntersection(r3, i);
            else System.out.println("Invalid Intersection!");
            return;
        }
        
        setIntersection(r2, r3);
        i = findIntersection(s2, s3);
        setIntersection(r1, i);
    }
    
    private void setIntersection(Role r1, Role r2) {
        Role r = null;
        if(r2 instanceof Intersection) {
            //Intersection in = (Intersection)r2;
            r = new Intersection3((Intersection)r2, r1);
        }
        
        else {
            r = new Intersection(r1, r2);
        }
      /*
      if( roleExists(r) != null)
      {
        r = null;
        return;
      }
       */
        
        r1.setDisplayable(false);
        r1.setVisible(false);
        r2.setDisplayable(false);
        r2.setVisible(false);
        
        if(!place(r)) {
            r1.setDisplayable(true);
            r1.setVisible(true);
            r2.setDisplayable(true);
            r2.setVisible(true);
            return;
        }
        
        System.out.println("Forming new intersection: " + r.getRoleName());
        
        roles.put(r.getRoleName(), r);
        
        repaint();
    }
    
    private Role roleExists(Role r) {
        //System.out.println("In roleExists for " + r.getRoleName());
        if(r == null) 
            return null;
        
        Object[] roles = getRoles();
        Role r1;
        for(int i = 0; i<roles.length; i++) {
            r1 = (Role)roles[i];
            if(r.equalsRole(r1)) 
                return r1;
        }
        
        //System.out.println("roleExists returning null");
        return null;
    }
    
    public void setRoleVisible(String name, boolean visible) {
        Object obj = roles.get(name);
        if(obj == null) return;
        Role role = (Role)obj;
        
        if(visible && !role.getVisible()) {
            if(!place(role)) 
                return;
        }
        
        role.setVisible(visible);
        repaint();
    }
    
    private boolean place(Role role) {
        Point p;
        if(!isEmptySpot(role.getX(), role.getY(), role.getWidth(), role.getHeight())) {
            p = getEmptySpot(role.getWidth(), role.getHeight());
        } else {
            p = new Point(role.getX(), role.getY());
        }
        
        role.setX((int)p.getX());
        role.setY((int)p.getY());
        
        return true;
    }
    
    void showNoSpace(String opName) {
        JOptionPane.showMessageDialog(null, "No empty space available to perform this "  + opName + " operation. " +
                "\n Please hide some sets.", "No more space", JOptionPane.ERROR_MESSAGE);
    }

    public Role getRoleAt(int x, int y) {
        Iterator<Object> itr = roles.values().iterator();
        while(itr.hasNext()) {
            Role role = (Role)itr.next();
            if(role.getVisible() && role.isRoleAt(x,y)) {
                return role;
            }
        }
        return (null);
    }
    
    void checkIntersections(Role excludeRole) {
        Role intersect;
        Iterator<Object> itr = roles.values().iterator();
        while(itr.hasNext()) {
            Role role = (Role)itr.next();
            if(!role.getVisible() || role.equals(excludeRole)) 
                continue;
            
            if(role.isIntersect(excludeRole)) {
                if(role instanceof Intersection3) 
                    return;
                
                ArrayList<String> list = new ArrayList(2);
                list.add(excludeRole.getRoleName());
                
                if(role instanceof Intersection) {
                    Intersection i = (Intersection)role;
                    list.add(i.getRole1().getRoleName());
                    list.add(i.getRole2().getRoleName());
                } else {
                    list.add(role.getRoleName());
                }
                
                int prevSize = roles.size();
                setIntersection(list);
                if(rcl != null && roles.size() > prevSize) {
                    rcl.setIntersection(list);
                }
                break;
            }
        }
    }
    
    private void drawAll(Graphics g) {
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        
        Iterator<Object> itr = roles.values().iterator();
        while(itr.hasNext()) {
            Role role = (Role)itr.next();
            role.draw(g);
        }
    }
    
    public void paint(Graphics g) {
        if(imG == null || imBuffer.getHeight(this) < this.getHeight() || imBuffer.getWidth(this) < this.getWidth()) {
            imBuffer = this.createImage(this.getWidth(), this.getHeight());
            imG = imBuffer.getGraphics();
            imG.setColor(Color.darkGray);
        }
        
        drawAll(imG);
        g.drawImage(imBuffer, 0, 0, this);
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    void mouseIsAt(Point point) {
        Role r;
        if( (r = getRoleAt(point.x, point.y)) != null) {
            Host h = r.getHostAt(point.x, point.y);
            if(h != null) setToolTipText(h.toString());
            else this.setToolTipText(r.getRoleName());
            
            if(h == null) {
                r.setResizeMode(point.x, point.y);
                setCursor(r.getResizeCursor(point.x, point.y));
                resizeRole = r.isResizable() ? r : null;
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        } else {
            if(getToolTipText() != "") {
                setToolTipText("");
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        
    }
    
    boolean isResizeRoleMode() {
        return resizeRole != null;
    }
    
    Role getResizeRole() {
        return resizeRole;
    }
    
    Role createRoleFromIntersection(Role r, int x, int y) {
        String name = r.getNewRoleNameAt(x,y);
        ArrayList<HostInfo> hosts = r.getHostsAt(x, y);
        if(rcl != null) rcl.removeIntersection(r.getRoleNames());
        if(rcl != null) rcl.insertRole(name, hosts);
        return(insertRole(name, hosts, x - Role.SIZE/2, y - Role.SIZE/2));
    }
    
    Role handleIntersectionDrag(Role r, int x, int y) {
        if(isResizeRoleMode()) 
            return null;
        
        if(!r.isAtUnique(x,y))
            return(createRoleFromIntersection(r, x, y));
        
        if(rcl != null) 
            rcl.removeIntersection(r.getRoleNames());
        
        Role moving = null;
        Role staying = null;
        
        if(r instanceof Intersection) {
            Intersection i = (Intersection)r;
            
            moving = i.getRoleAt(x,y);
            staying = i.getRole1().isRoleAt(x,y) ? i.getRole2() : i.getRole1();
            
            i = null;
            staying.setDraggable(true);
        } else if(r instanceof Intersection3) {
            Intersection3 i = (Intersection3)r;
            
            moving = i.getRoleAt(x, y);
            
            Role staying1 = i.getRole1();
            if(moving == staying1) staying1 = i.getRole2();
            if(moving == staying1) staying1 = i.getRole3();
            
            Role staying2 = i.getRole1();
            if(moving == staying2 || staying1 == staying2) staying2 = i.getRole2();
            if(moving == staying2 || staying1 == staying2) staying2 = i.getRole3();
            System.out.println(moving.getRoleName() + " " + staying1.getRoleName() +" " + staying2.getRoleName());
            
            staying = (Role)roles.get(Intersection.makeName(staying1.getRoleName(), staying2.getRoleName()));
            if(staying == null)
                staying = (Role)roles.get(Intersection.makeName(staying2.getRoleName(), staying1.getRoleName()));
            
            if(staying == null) {
                Intersection intr = i.getIntersection();
                removeRole(intr.getRoleName());
                intr = null;
                staying = new Intersection(staying1, staying2);
                roles.put(staying.getRoleName(), staying);
            }
            
            staying.setX(Math.min(Math.min(moving.getX(), staying1.getX()), staying2.getX()));
            staying.setY(Math.min(Math.min(moving.getY(), staying1.getY()), staying2.getY()));
        }
        
        //System.out.println("Staying " + staying.getRoleName());
        
        removeRole(r.getRoleName());
        r.setDisplayable(false);
        r.setVisible(false);
        staying.setVisible(false);
        moving.setVisible(false);
        place(staying);
        
        moving.setDisplayable(true);
        moving.setVisible(true);
        moving.setDraggable(true);
        staying.setDisplayable(true);
        staying.setVisible(true);
        r = null;
        
        if(rcl != null && staying.getRoleNames().size() > 1) {
            rcl.setIntersection(staying.getRoleNames());
	}
    
        return moving;
    }
    
    Intersection findIntersection(String r1, String r2, String r3) {
        Intersection i = null;
        if((i = findIntersection(r1, r2)) != null) return i;
        if((i = findIntersection(r1, r3)) != null) return i;
        return findIntersection(r2, r3);
    }
    
    Intersection findIntersection(String r1, String r2) {
        if(roles == null) return(null);
        
        Object obj = roles.get(Intersection.makeName(r1, r2));
        if(obj != null) return (Intersection)(Role)obj;
        
        obj = roles.get(Intersection.makeName(r2, r1));
        if(obj != null) return (Intersection)(Role)obj;
        
        return null;
    }
    
    ArrayList<Role> findRoles(String r1, String r2) {
        if(roles == null) return(null);
        Object obj[] = getRoles();
        ArrayList<Role> rs = new ArrayList<Role>();
        for(int i = 0; i <obj.length; i++) {
            Role r = (Role)obj[i];
            String name = r.getRoleName();
            if(name.indexOf(r1) != -1 && name.indexOf(r2) != -1) rs.add(r);
        }
        return (rs);
    }
    
    ArrayList<Role> findRoles(String r1) {
        if(roles == null) return(null);
        Object obj[] = getRoles();
        ArrayList<Role> rs = new ArrayList<Role>();
        for(int i = 0; i <obj.length; i++) {
            Role r = (Role)obj[i];
            String name = r.getRoleName();
            if(name.indexOf(r1) != -1) rs.add(r);
        }
        return (rs);
    }
    
    Intersection3 findIntersection3(String r1, String r2, String r3) {
        if(roles == null) 
		return null;
        
        Object obj = roles.get(Intersection3.makeName(r1, r2, r3));
        if(obj != null) 
            return (Intersection3)obj;
        
        obj = roles.get(Intersection3.makeName(r1, r3, r2));
        if(obj != null) 
            return (Intersection3)obj;
        
        obj = roles.get(Intersection3.makeName(r2, r1, r2));
        if(obj != null) 
            return (Intersection3)obj;
        
        obj = roles.get(Intersection3.makeName(r2, r3, r1));
        if(obj != null) 
            return (Intersection3)obj;
        
        obj = roles.get(Intersection3.makeName(r3, r1, r2));
        if(obj != null) 
            return (Intersection3)obj;
        
        obj = roles.get(Intersection3.makeName(r3, r2, r1));
        if(obj != null) 
            return (Intersection3)obj;
        
        return null;
    }
    
    void fitRoles() {
        Object[] objs = getRoles();
        for(int i = 0; i<objs.length; i++) {
            Role r = (Role)objs[i];
            
            if(r.getX() + r.getWidth()/2 > this.getWidth() || r.getY() + r.getHeight()/2 > this.getHeight()) {
                r.setVisible(false);
            }
        }
    }
    
    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }
    
    public void componentHidden(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
    }
}
