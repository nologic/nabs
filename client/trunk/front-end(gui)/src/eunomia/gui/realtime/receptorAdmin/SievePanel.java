/*
 * SievePanel.java
 *
 * Created on April 23, 2007, 8:43 PM
 *
 */

package eunomia.gui.realtime.receptorAdmin;

import eunomia.core.managers.ModuleDescriptor;
import eunomia.core.managers.event.state.AddDatabaseEvent;
import eunomia.core.managers.event.state.AddDatabaseTypeEvent;
import eunomia.core.managers.event.state.AddModuleEvent;
import eunomia.core.managers.event.state.AddStreamServerEvent;
import eunomia.core.managers.event.state.ReceptorUserAddedEvent;
import eunomia.core.managers.event.state.ReceptorUserRemovedEvent;
import eunomia.core.managers.event.state.RemoveDatabaseEvent;
import eunomia.core.managers.event.state.RemoveStreamServerEvent;
import eunomia.core.managers.event.state.StreamStatusChangedEvent;
import eunomia.core.managers.listeners.ReceptorStateListener;
import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.msg.rsp.admin.AdminStatusMessage;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SievePanel extends JPanel implements ActionListener, ReceptorStateListener {
    private Receptor receptor;
    
    private JButton addUser;
    private JButton delUser;
    private JButton rstUser;
    
    private JList usersList;
    private JList modulesList;
    
    public SievePanel(Receptor rec) {
        receptor = rec;
        
        addControls();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        String user = (String)usersList.getSelectedValue();
        if(o == addUser) {
            setPass(null);
        } else if(o == delUser) {
            receptor.getOutComm().deleteReceptorUser(user);
        } else if(o == rstUser) {
            setPass(user);
        }
    }
    
    private void setPass(String user) {
        String pass;
        
        if(user == null) {
            user = JOptionPane.showInputDialog(this, "Enter user name");
        }
        
        if(user != null) {
            pass = JOptionPane.showInputDialog(this, "Enter password");
            
            if(pass != null) {
                receptor.getOutComm().setReceptorUser(user, pass, null);
            }
        }
    }

    private void addControls() {
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2));
        JPanel usersPanel = new JPanel(new BorderLayout());
        JPanel modulesPanel = new JPanel(new BorderLayout());
        
        setLayout(new GridLayout(1, 2));
        
        buttonsPanel.add(addUser = new JButton("Add User"));
        buttonsPanel.add(delUser = new JButton("Remove User"));
        buttonsPanel.add(rstUser = new JButton("Reset User Password"));
        
        JScrollPane listScroll = new JScrollPane(usersList = new JList());
        usersPanel.add(listScroll);
        usersPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        JScrollPane modScroll = new JScrollPane(modulesList = new JList());
        modulesPanel.add(modScroll);
        
        add(usersPanel);
        add(modulesPanel);
        
        addUser.addActionListener(this);
        delUser.addActionListener(this);
        rstUser.addActionListener(this);
        
        listScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Users List"));
        modScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Modules List"));
    }

    public void databaseAdded(AddDatabaseEvent e) {
    }

    public void databaseRemoved(RemoveDatabaseEvent e) {
    }

    public void databaseTypeAdded(AddDatabaseTypeEvent e) {
    }

    public void moduleAdded(AddModuleEvent e) {
        List mods = receptor.getState().getReceptorModules();
        Iterator it = mods.iterator();
        String[] data = new String[mods.size()];
        int i = 0;
        while (it.hasNext()) {
            AdminStatusMessage.ModDesc md = (AdminStatusMessage.ModDesc) it.next();
            data[i++] = "(" + ModuleDescriptor.types[md.getType()] + ") " + md.getName() + " - " + md.getDescription();
        }
        modulesList.setListData(data);
    }

    public void streamServerAdded(AddStreamServerEvent e) {
    }

    public void streamServerRemoved(RemoveStreamServerEvent e) {
    }

    public void streamStatusChanged(StreamStatusChangedEvent e) {
    }

    public void receptorUserAdded(ReceptorUserAddedEvent e) {
        Object sel = usersList.getSelectedValue();
        
        usersList.setListData(receptor.getState().getReceptorUsers().toArray());
        usersList.setSelectedValue(sel, true);
    }

    public void receptorUserRemoved(ReceptorUserRemovedEvent e) {
    }
}