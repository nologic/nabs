/*
 * FocusManager.java
 *
 * Created on August 14, 2006, 9:26 PM
 */

package eunomia.gui.desktop.icon;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DesktopFocusManager {
    private DesktopItem curSel;
    
    public DesktopFocusManager() {
    }
    
    public void setFocused(DesktopItem sel){
        if(curSel != null){
            curSel.setSelected(false);
        }
        curSel = sel;
        curSel.setSelected(true);
    }
    
    public DesktopItem getFocused(){
        return curSel;
    }
}
