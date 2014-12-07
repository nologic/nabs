/*
 * DesktopItem.java
 *
 * Created on August 12, 2006, 3:01 PM
 *
 */

package eunomia.gui.desktop.icon;

import eunomia.gui.desktop.interfaces.DesktopIcon;
import eunomia.gui.desktop.interfaces.DesktopIconListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DesktopItem extends JLabel implements MouseListener, DesktopIconListener {
    public static final int SQ_WIDTH = 100;
    
    private static Dimension prefSize;
    private static Font selected_font, unselected_font;
    private static Color selected_color, unselected_color;
    
    private DesktopIcon dIcon;
    private DesktopFocusManager fMan;
    
    static {
        prefSize = new Dimension(SQ_WIDTH, SQ_WIDTH);
        selected_font = new Font("SansSerif", Font.BOLD, 16);
        unselected_font = new Font("SansSerif", Font.PLAIN, 16);
        selected_color = Color.GREEN;
        unselected_color = Color.BLACK;
    }
    
    public DesktopItem(DesktopIcon icon) {
        Font font = this.getFont();
        
        this.setHorizontalAlignment(JLabel.CENTER);
        this.setHorizontalTextPosition(JLabel.CENTER);
        this.setVerticalTextPosition(JLabel.BOTTOM);
        this.setFont(new Font(font.getName(), Font.PLAIN, 14));
        this.addMouseListener(this);
        this.setSize(SQ_WIDTH, SQ_WIDTH);
        this.setPreferredSize(prefSize);
        
        icon.addDesktopIconListener(this);
        
        setIcon(icon);
        setSelected(false);
    }
    
    public void setIcon(DesktopIcon icon){
        if(icon != null){
            dIcon = icon;
            this.setIcon(icon.getIcon());
            this.setText(icon.getName());
            this.setToolTipText(icon.getTooltip());
            this.setComponentPopupMenu(icon.getContextMenu());
        }
    }
    
    public void setFocusMananger(DesktopFocusManager mn){
        fMan = mn;
    }
    
    public void setSelected(boolean val){
        this.setFont((val?selected_font:unselected_font));
        this.setForeground((val?selected_color:unselected_color));
    }

    public void mouseClicked(MouseEvent e) {
        fMan.setFocused(this);
        if(e.getClickCount() == 2){
            dIcon.activate();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void iconChanged(DesktopIcon icon) {
        this.setIcon(icon.getIcon());
        this.setText(icon.getName());
        this.setToolTipText(icon.getTooltip());
        this.setComponentPopupMenu(icon.getContextMenu());
    }
}