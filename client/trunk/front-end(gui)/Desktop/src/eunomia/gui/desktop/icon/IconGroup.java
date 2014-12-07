/*
 * IconGroup.java
 *
 * Created on August 12, 2006, 3:17 PM
 *
 */

package eunomia.gui.desktop.icon;

import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class IconGroup extends JPanel {
    private List icons;
    private DesktopFocusManager fMan;
    
    public IconGroup() {
        icons = new LinkedList();
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);
    }
    
    public void setFocusManager(DesktopFocusManager man){
        fMan = man;
        Iterator it = icons.iterator();
        while (it.hasNext()) {
            DesktopItem item = (DesktopItem) it.next();
            item.setFocusMananger(man);
        }
    }

    public void setTitle(String title) {
        //setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title + ":"));
    }
    
    public void addItem(DesktopItem item){
        item.setFocusMananger(fMan);
        icons.add(item);
        add(item);
        update();
    }
    
    public void removeItem(DesktopItem item){
        icons.remove(item);
        remove(item);
        update();
    }
    
    public void update(){
        double hSize = (double)icons.size()/2.0;
        int hw = (int)(hSize * DesktopItem.SQ_WIDTH);
        if(icons.size() > 2){
            if(hw % DesktopItem.SQ_WIDTH > 0){
                hw += DesktopItem.SQ_WIDTH - (hw % DesktopItem.SQ_WIDTH);
            }
            hw += 20;
            this.setSize(hw, hw);
        } else {
            this.setSize(120, 240);
        }
        
        validate();
        repaint();
    }
}