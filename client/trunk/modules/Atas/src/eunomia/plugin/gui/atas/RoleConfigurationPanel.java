/*
 * RoleConfigurationPanel.java
 *
 * Created on April 11, 2007, 8:57 PM
 *
 */

package eunomia.plugin.gui.atas;

import eunomia.plugin.com.atas.ClassifierConfigurationMessage;
import eunomia.plugin.com.atas.ConfigurationInterface;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class RoleConfigurationPanel extends JPanel {
    private Map map = new HashMap();
    private JPanel configsPanel;
    
    public RoleConfigurationPanel() {
        configsPanel = new JPanel();
        
        setLayout(new BorderLayout());
        
        configsPanel.setLayout(new BoxLayout(configsPanel, BoxLayout.Y_AXIS));
        add(configsPanel, BorderLayout.NORTH);
    }
    
    public void addMessage(ClassifierConfigurationMessage msg) {
        ConfigurationInterface config = getConfig(msg.getRoleNumber(), msg.getClassName());
        
        if(config != null) {
            config.setConfigurationMessage(msg);
            configsPanel.add((JComponent)config);
            revalidate();
            repaint();
        }
    }
    
    public List getMessageList() {
        List list = new LinkedList();
        
        Iterator it = map.values().iterator();
        while (it.hasNext()) {
            ConfigurationInterface config = (ConfigurationInterface) it.next();
            list.add(config.getConfigurationMessage());
        }
        
        return list;
    }
    
    private ConfigurationInterface getConfig(int num, String klass) {
        String key = num + klass;
        ConfigurationInterface config = (ConfigurationInterface)map.get(key);
        if(config == null) {
            try {
                config = loadConfigurator(klass);
                map.put(key, config);
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        
        return config;
    }
    
    private static ConfigurationInterface loadConfigurator(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String className = "eunomia.plugin.gui.atas.classifiers." + name + "Config";
        Class klass = Class.forName(className);
        
        return (ConfigurationInterface)klass.newInstance();
    }
}