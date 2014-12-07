/*
 * StackedBarRenderer3DMod.java
 *
 * Created on August 15, 2005, 4:23 PM
 *
 */

package eunomia.plugin.gui.lossyHistogram;

import java.awt.*;
import java.awt.geom.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.ui.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class StackedBarRenderer3DMod extends StackedBarRenderer3D {
    private Rectangle2D.Double dRect;
    /**
     * Draws a stacked bar (with 3D-effect) for a specific item.
     * Mike: Modified JFreeChart code, does in one pass, draws 0 values,
     *       draws labels only for last row.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the plot area.
     * @param plot  the plot.
     * @param domainAxis  the domain (category) axis.
     * @param rangeAxis  the range (value) axis.
     * @param dataset  the data.
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
            CategoryItemRendererState state,
            Rectangle2D dataArea,
            CategoryPlot plot,
            CategoryAxis domainAxis,
            ValueAxis rangeAxis,
            CategoryDataset dataset,
            int row,
            int column,
            int pass) {
        
        if(dRect == null){
            dRect = new Rectangle2D.Double();
        }
        // check the value we are plotting...
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null) {
            return;
        }
        
        double value = dataValue.doubleValue();
        
        
        dRect.setRect(
                dataArea.getX(), dataArea.getY() + getYOffset(),
                dataArea.getWidth() - getXOffset(),
                dataArea.getHeight() - getYOffset()
                );
                
        Rectangle2D adjusted = dRect;
        PlotOrientation orientation = plot.getOrientation();
        
        double barW0 = domainAxis.getCategoryMiddle(
                column, getColumnCount(), adjusted, plot.getDomainAxisEdge()
                ) - state.getBarWidth() / 2.0;
        
        double positiveBase = 0.0;
        double negativeBase = 0.0;
        for (int i = 0; i < row; i++) {
            Number v = dataset.getValue(i, column);
            if (v != null) {
                double d = v.doubleValue();
                if (d > 0) {
                    positiveBase = positiveBase + d;
                } else {
                    negativeBase = negativeBase + d;
                }
            }
        }
        
        double translatedBase;
        double translatedValue;
        RectangleEdge location = plot.getRangeAxisEdge();
        if (value >= 0.0) {
            translatedBase = rangeAxis.valueToJava2D(positiveBase, adjusted, location);
            translatedValue = rangeAxis.valueToJava2D(positiveBase + value, adjusted, location);
        } else {
            translatedBase = rangeAxis.valueToJava2D(negativeBase, adjusted, location);
            translatedValue = rangeAxis.valueToJava2D(negativeBase + value, adjusted, location);
        }
        double barL0 = Math.min(translatedBase, translatedValue);
        double barLength = Math.max(Math.abs(translatedValue - translatedBase), getMinimumBarLength());
        
        Rectangle2D bar = new Rectangle2D.Double();
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar.setRect(barL0, barW0, barLength, state.getBarWidth());
        } else {
            bar.setRect(barW0, barL0, state.getBarWidth(), barLength);
        }
        Paint itemPaint = getItemPaint(row, column);
        g2.setPaint(itemPaint);
        g2.fill(bar);
        
        double x0 = bar.getMinX();
        double x1 = x0 + getXOffset();
        double x2 = bar.getMaxX();
        double x3 = x2 + getXOffset();
        
        double y0 = bar.getMinY() - getYOffset();
        double y1 = bar.getMinY();
        double y2 = bar.getMaxY() - getYOffset();
        double y3 = bar.getMaxY();
        
        GeneralPath bar3dRight = null;
        GeneralPath bar3dTop = null;
        if (value >= 0.0 || orientation == PlotOrientation.VERTICAL) {
            bar3dRight = new GeneralPath();
            bar3dRight.moveTo((float) x2, (float) y3);
            bar3dRight.lineTo((float) x2, (float) y1);
            bar3dRight.lineTo((float) x3, (float) y0);
            bar3dRight.lineTo((float) x3, (float) y2);
            bar3dRight.closePath();
            
            if (itemPaint instanceof Color) {
                g2.setPaint(((Color) itemPaint).darker());
            }
            g2.fill(bar3dRight);
        }
        
        if (value >= 0.0 || orientation == PlotOrientation.HORIZONTAL) {
            bar3dTop = new GeneralPath();
            bar3dTop.moveTo((float) x0, (float) y1);
            bar3dTop.lineTo((float) x1, (float) y0);
            bar3dTop.lineTo((float) x3, (float) y0);
            bar3dTop.lineTo((float) x2, (float) y1);
            bar3dTop.closePath();
            g2.fill(bar3dTop);
        }
        
        if (isDrawBarOutline() && state.getBarWidth() > 3) {
            g2.setStroke(getItemOutlineStroke(row, column));
            g2.setPaint(getItemOutlinePaint(row, column));
            g2.draw(bar);
            if (bar3dRight != null) {
                g2.draw(bar3dRight);
            }
            if (bar3dTop != null) {
                g2.draw(bar3dTop);
            }
        }
        
        // collect entity and tool tip information...
        if (state.getInfo() != null && state.getInfo().getOwner() != null) {
            EntityCollection entities = state.getInfo().getOwner().getEntityCollection();
            if (entities != null) {
                String tip = null;
                CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
                if (tipster != null) {
                    tip = tipster.generateToolTip(dataset, row, column);
                }
                String url = null;
                if (getItemURLGenerator(row, column) != null) {
                    url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
                }
                CategoryItemEntity entity = new CategoryItemEntity(bar, tip, url, dataset, row, dataset.getColumnKey(column), column);
                entities.add(entity);
            }
        }
        CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
        if (generator != null && isItemLabelVisible(row, column) && row == dataset.getRowCount() - 1) {
            drawItemLabel(g2, dataset, row, column, plot, generator, bar, (value < 0.0));
        }
    }
    
    /**
     * Returns the number of passes through the dataset required by the
     * renderer.  This method returns <code>2</code>, the second pass is used
     * to draw the item labels.
     *
     * @return The pass count.
     */
    public int getPassCount() {
        return 1;
    }
}
