/*
 * PolicyViewer.java
 *
 * Created on June 19, 2007, 10:41 PM
 *
 */

package eunomia.plugin.gui.networkPolicy;

import com.vivic.eunomia.filter.Filter;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.utils.networkPolicy.PolicyLanguage;
import eunomia.util.SpringUtilities;
import com.vivic.eunomia.sys.util.Util;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

/**
 *
 * @author Mikhail Sosonkin
 */
public class PolicyViewer extends JPanel {
    private JTextArea desc, filterDesc;
    private JLabel type, rate, rateLabel;
    private JList list; // for rendering
    
    public PolicyViewer() {
        list = new JList();
        
        addControls();
        
        showPolicy(null);
    }
    
    public void showPolicy(PolicyItem pi) {
        if(pi != null) {
            type.setText(PolicyLanguage.TYPE_NAME[pi.getPolicyType()]);
            desc.setText(pi.getDescription());
            
            String text = null;
            Filter f = pi.getFilter();
            String[] basic = PolicyLanguage.makeFilterBasicDescription(f);
            if(basic != null) {
                text = (basic[0].equals("")?"":"From:\n" + basic[0] + "\n") +
                       (basic[1].equals("")?"":"To:\n" + basic[1] + "\n");
            } else {
                text = PolicyLanguage.makeFilterDescription(f);
            }
            filterDesc.setText(text);
            
            long r = pi.getRate();
            if(pi.getPolicyType() == PolicyItem.REAL_TIME) {
                rate.setText(Util.convertBytesRate((double)r, true));
                rateLabel.setText("Rate Limit:");
            } else {
                rate.setText(Util.convertBytes((double)r, true));
                rateLabel.setText("Limit:");
            }
        } else {
            type.setText("");
            rate.setText("");
            desc.setText("");
            filterDesc.setText("");
        }
    }
    
    private void addControls() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel leftTopPanel = new JPanel(new SpringLayout());
        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel viewerPanel = new JPanel();
        
        viewerPanel.setLayout(new BoxLayout(viewerPanel, BoxLayout.Y_AXIS));
                
        setLayout(new BorderLayout(5, 5));
        
        JLabel l;
        leftTopPanel.add(l = new JLabel("Type:"));
        l.setHorizontalAlignment(JLabel.RIGHT);
        leftTopPanel.add(type = new JLabel());
        leftTopPanel.add(l = new JLabel("Limit:"));
        l.setHorizontalAlignment(JLabel.RIGHT);
        rateLabel = l;
        leftTopPanel.add(rate = new JLabel());
        SpringUtilities.makeCompactGrid(leftTopPanel, leftTopPanel.getComponentCount()/2, 2, 2, 2, 4, 4);
        
        leftPanel.add(leftBottomPanel, BorderLayout.CENTER);
        leftPanel.add(leftTopPanel, BorderLayout.NORTH);

        rightPanel.add(new JScrollPane(desc = new JTextArea(3, 10)));
        
        JScrollPane sPane;
        filterPanel.add(sPane = new JScrollPane(filterDesc = new JTextArea(7, 10)));
        sPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        viewerPanel.add(leftPanel);
        viewerPanel.add(rightPanel);
        viewerPanel.add(filterPanel);
        add(viewerPanel);
        
        Border etched = BorderFactory.createEmptyBorder();
        rightPanel.setBorder(BorderFactory.createTitledBorder(etched, "Description"));
        filterPanel.setBorder(BorderFactory.createTitledBorder(etched, "Filter"));
        setBorder(BorderFactory.createTitledBorder("Policy Details:"));
        
        desc.setEditable(false);
        desc.setLineWrap(true);
    }
}
