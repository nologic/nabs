/*
 * RealtimeSummaryViewer.java
 *
 * Created on June 13, 2005, 12:30 PM
 */

package eunomia.gui.realtime;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class RealtimeSummaryViewer extends JPanel {
    public RealtimeSummaryViewer() {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Streaming Data Summary"));
    }
}