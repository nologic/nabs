/*
 * DistributionCell.java
 *
 * Created on April 24, 2006, 9:11 PM
 */

package eunomia.plugin.gui.hostDetails;

import eunomia.config.Settings;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DistributionCell extends JPanel implements TableCellRenderer {
    private double[] percent;
    
    public DistributionCell() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        percent = (double[])value;
        
        return this;
    }
    
    public void paint(Graphics g){
        Dimension dim = this.getSize();
        int lastX = 2;
        int length = (int)dim.getWidth() - 4;
        int height = (int)dim.getHeight() - 4;
        
        for(int i = 0; i < percent.length; i++){
            int rectWidth = (int)(((double)length) * percent[i]);
            if(i == percent.length - 1){
                rectWidth = length - lastX + 1;
            }
            
            g.setColor(Settings.getTypeColor(i));
            g.fillRect(lastX, 2, rectWidth, height);
            lastX += rectWidth;
        }
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(1, 1, length + 1, height + 1);
    }
}