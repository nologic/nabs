/*
 * PolicyManagementPane.java
 *
 * Created on January 12, 2007, 3:15 PM
 */

package eunomia.plugin.gui.networkPolicy;

import com.vivic.eunomia.sys.frontend.ConsoleModuleManager;
import com.vivic.eunomia.filter.Filter;
import eunomia.gui.filter.FilterEditorPanel;
import eunomia.plugin.com.networkPolicy.PolicyItem;
import eunomia.plugin.utils.networkPolicy.PolicyLanguage;
import eunomia.plugin.utils.networkPolicy.PolicyLanguageParser;
import eunomia.receptor.module.NABFlow.NABFilterEntry;
import eunomia.receptor.module.NABFlow.NABFlow;
import eunomia.util.SpringUtilities;
import com.vivic.eunomia.sys.util.Util;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author  kulesh, Mikhail Sosonkin
 */
public class PolicyEditor extends JPanel implements ActionListener {
    private FilterEditorPanel filterEditor;
    private JTextArea policyDescription;
    private JComboBox policyType;
    private PolicyItem item;
    private RateEditor rateEditor;
    private BasicFilterEditor basicEditor;
    private ConsoleModuleManager manager;
    private JTabbedPane tabs;

    public PolicyEditor() {
        addControls();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if(o == policyType) {
            rateEditor.setPolicyType(policyType.getSelectedIndex());
        }
    }
    
    public void setModuleManager(ConsoleModuleManager man) {
        manager = man;
    }
    
    private void addControls() {
        JPanel fieldsPanel = new JPanel();
        JPanel descPanel = new JPanel(new BorderLayout());
        JPanel idPanel;
        JLabel jPolicyNameLabel;
        JPanel ratePanel = new JPanel(new BorderLayout(5, 0));
        JLabel jPolicyTypeLabel;
        tabs = new JTabbedPane();
        JPanel typesPanel;
        
        idPanel = new JPanel(new SpringLayout());
        jPolicyTypeLabel = new JLabel();
        policyType = new JComboBox(new String[] { "Real Time", "Hourly", "Daily", "Weekly", "Monthly" });
        filterEditor = new FilterEditorPanel();
        
        setLayout(new BorderLayout());
        
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        
        idPanel.add(jPolicyTypeLabel = new JLabel("Policy Type:"));
        idPanel.add(policyType);
        idPanel.add(new JLabel("Data Rate:"));
        idPanel.add(rateEditor = new RateEditor());
        SpringUtilities.makeCompactGrid(idPanel, idPanel.getComponentCount()/2, 2, 10, 10, 8, 8);
        descPanel.add(new JScrollPane(policyDescription = new JTextArea(4, 20)), BorderLayout.CENTER);
        
        fieldsPanel.add(idPanel);
        fieldsPanel.add(descPanel);

        tabs.add("Basic", basicEditor = new BasicFilterEditor());
        tabs.add("Granular", filterEditor);
        tabs.add("Basic Help", new BasicEditorHelp());
        
        add(fieldsPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        
        ratePanel.setBorder(BorderFactory.createTitledBorder("Transfer Rate"));
        descPanel.setBorder(BorderFactory.createTitledBorder("Description of Policy"));
        tabs.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Filter Editor"));
        tabs.setTabPlacement(JTabbedPane.BOTTOM);
        
        policyType.addActionListener(this);
    }
    
    public void setPolicyItem(PolicyItem pi, boolean editAll, boolean isNew){
        item = pi;
        StringBuilder b = new StringBuilder();
        Filter filter = pi.getFilter();
        
        filterEditor.setFilter(filter, manager, new String[]{"NABFlow"});
        policyDescription.setText(pi.getDescription());
        rateEditor.setPolicyType(pi.getPolicyType());
        rateEditor.setRate(pi.getRate());
        policyType.setSelectedIndex(pi.getPolicyType());
        
        basicEditor.setFilter(pi.getFilter());
        if(basicEditor.isBasic()) {
            tabs.setSelectedIndex(0);
        } else {
            tabs.setSelectedIndex(1);
        }
        
        policyType.setEnabled(isNew);
        rateEditor.setEnabled(editAll);
        filterEditor.setEnabled(editAll);
    }
    
    public boolean commitData() {
        Filter filter;
        long newRate = 0;
        if(filterEditor.isModified()) {
            filter = filterEditor.getFilter();
        } else {
            try {
                filter = basicEditor.getFilter();
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Unable to parse basic filter:\n\t" + ex.getMessage());
                return false;
            }
        }

        try {
            newRate = rateEditor.getRate();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Unable to parse rate:\n\t" + ex.getMessage());
            return false;
        }

        item.setFilter(filter);
        item.setPolicyType(policyType.getSelectedIndex());
        item.setRate(newRate);
        item.setDescription(policyDescription.getText());
        
        return true;
    }
    
    private class RateEditor extends JPanel {
        private JTextField rate;
        private int polType;
        
        public RateEditor() {
            addControls();
        }
        
        public void setPolicyType(int t) {
            polType = t;
        }
        
        public void setEnabled(boolean v) {
            rate.setEnabled(v);
        }
        
        private void addControls() {
            JPanel topPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            
            setLayout(new BorderLayout(5, 5));
            
            topPanel.add(rate = new JTextField());
            
            add(topPanel, BorderLayout.CENTER);
        }
        
        public void setRate(long r) {
            if(polType == PolicyItem.REAL_TIME) {
                rate.setText(Util.convertBytesRate((double)r, true));
            } else {
                rate.setText(Util.convertBytes((double)r, true));
            }
        }
        
        public long getRate() throws ParseException {
            return Util.parseBytes(rate.getText());
        }
    }
    
    private class BasicFilterEditor extends JPanel {
        private Filter filter;
        private JTextArea fromArea;
        private JTextArea toArea;
        private JCheckBox[] typeBoxes;
        private boolean isBasic;
        
        public BasicFilterEditor() {
            isBasic = false;
            addControls();
        }
        
        public void setFilter(Filter filter) {
            this.filter = filter;
            String[] basic = PolicyLanguage.makeFilterBasicDescription(filter);
            
            isBasic = basic != null;
            
            fromArea.setEnabled(isBasic);
            toArea.setEnabled(isBasic);
            for (int i = 0; i < typeBoxes.length; i++) {
                typeBoxes[i].setEnabled(isBasic);
            }
            
            if(isBasic) {
                fromArea.setText(basic[0]);
                toArea.setText(basic[1]);
                
                if(filter.getWhiteList().getCount() != 0) {
                    NABFilterEntry entry = (NABFilterEntry)filter.getWhiteList().getAsArray()[0];
                    setAllowList(entry.getAllowType());
                } else {
                    setAllowList(new boolean[]{true, true, true, true, true, true, true, true});
                }
            }
        }
        
        public boolean isBasic() {
            return isBasic;
        }
        
        public Filter getFilter() throws ParseException {
            PolicyLanguageParser.parseBasicFilter(filter, getFrom(), getTo(), getAllowList());

            return filter;
        }
        
        public String getFrom() {
            return fromArea.getText();
        }
        
        public String getTo() {
            return toArea.getText();
        }
        
        public boolean[] getAllowList() {
            boolean[] list = new boolean[NABFlow.NUM_TYPES];
            for (int i = 0; i < list.length; i++) {
                list[i] = typeBoxes[i].isSelected();
            }
            return list;
        }
        
        public void setAllowList(boolean[] list) {
            for (int i = 0; i < list.length; i++) {
                typeBoxes[i].setSelected(list[i]);
            }
        }
        
        private void addControls() {
            JPanel typesPanel = new JPanel(new GridLayout(3, 3));
            JPanel toFromPanel = new JPanel(new GridLayout(2, 1));
            
            setLayout(new BorderLayout());
            typeBoxes = new JCheckBox[NABFlow.NUM_TYPES];
            for (int i = 0; i < typeBoxes.length; i++) {
                typeBoxes[i] = new JCheckBox(NABFlow.typeNames[i]);
                typesPanel.add(typeBoxes[i]);
            }
            typesPanel.add(new JPanel());
            
            toFromPanel.add(new JScrollPane(fromArea = new JTextArea()));
            toFromPanel.add(new JScrollPane(toArea = new JTextArea()));
            
            add(typesPanel, BorderLayout.NORTH);
            add(toFromPanel);
            
            fromArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            toArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            fromArea.setBorder(BorderFactory.createTitledBorder("From:"));
            toArea.setBorder(BorderFactory.createTitledBorder("To:"));
            
            setBorder(BorderFactory.createTitledBorder("Types To Consider"));
        }
    }
    
    private class BasicEditorHelp extends JPanel {
        private JTextArea helpText;
        
        public BasicEditorHelp() {
            StringBuilder text = new StringBuilder();
            text.append("Basic filter accepts 2 fields, To and From.\n");
            text.append("Both fields accept IP and Port ranges.\n");
            text.append("The format is [IP range]:[PORT range], [PORT range], ...\n\n");
            text.append("For example:\n");
            text.append("\tTO:\n");
            text.append("\t192.168.*.10-100:6667,8080\n");
            text.append("\tFROM:\n");
            text.append("\t172.16-31.*.*:0-1024\n");
            
            addControls();
            
            helpText.setText(text.toString());
        }
        
        private void addControls() {
            helpText = new JTextArea();
            setLayout(new BorderLayout());
            
            add(new JScrollPane(helpText));
            
            helpText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        }
    }
}