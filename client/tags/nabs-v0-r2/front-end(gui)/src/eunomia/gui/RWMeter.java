/*
 * RWMeter.java
 *
 * Created on November 28, 2006, 3:27 PM
 *
 */

package eunomia.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JComponent;
import eunomia.util.oo.listeners.NabObjectOutputListener;
import eunomia.util.oo.listeners.NabObjectInputListener;
import eunomia.util.oo.NabObjectOutput;
import eunomia.util.oo.NabObjectInput;
import eunomia.util.Util;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RWMeter extends JComponent implements NabObjectInputListener, NabObjectOutputListener {
    private static Color general_color;
    private static Color read_color;
    private static Color write_color;

    private Color read;
    private Color write;
    private Dimension size;

    static {
        general_color = Color.GRAY;
        read_color = Color.GREEN;
        write_color = Color.RED;
    }

    public RWMeter() {
        read = write = general_color;
        size = new Dimension(32, 0);

        NabObjectInput.setNabObjectInputListener(this);
        NabObjectOutput.setNabObjectOutputListener(this);
    }

    public Dimension getPreferredSize() {
        return size;
    }

    public void paint(Graphics g){
        int h = getHeight();
        int half_h = h/2;

        g.setColor(read);
        g.fillOval(half_h, half_h - 4, half_h + 2, half_h + 2);
        g.setColor(write);
        g.fillOval(h + 3, half_h - 4, half_h + 2, half_h + 2);
    }

    public void beginReadingObject(NabObjectInput out) {
        read = read_color;
        repaint();
    }

    public void endReadingObject(NabObjectInput out, Object o) {
        read = general_color;
        repaint();
    }

    public void beginWrittingObject(NabObjectOutput out, Object o) {
        write = write_color;
        repaint();
    }

    public void endWrittingObject(NabObjectOutput out, Object o) {
        write = general_color;
        repaint();
    }
}