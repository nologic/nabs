/*
 * ReportViewer.java
 *
 * Created on December 5, 2006, 10:22 PM
 *
 */

package eunomia.gui.module;

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
import eunomia.gui.IconResource;
import eunomia.gui.NABStrings;
import eunomia.messages.DatabaseDescriptor;
import eunomia.module.AnlzFrontendModule;
import com.vivic.eunomia.module.frontend.AnlzFrontendListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnalysisPortal extends ModulePortal {
    private String name;
    private AnlzFrontendModule module;
    private JPanel holder;
    private JPanel reportPanel;
    private JPanel setupPanel;
    private JButton detach;
    private JButton start;

    private static Logger logger;
    
    static {
        logger = Logger.getLogger(AnalysisPortal.class);
    }
    
    public AnalysisPortal(AnlzFrontendModule inst) {
        super(inst);
        module = inst;
        
        localAddControls();
    }
    
    public void terminate(){
        int ans = JOptionPane.showConfirmDialog(this, 
                "Closing this module portal will terminate the instance on the " + NABStrings.CURRENT_RECEPTOR_NAME + ". Are you sure you want to proceed?",
                "Should I proceed?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if(ans == JOptionPane.YES_OPTION) {
            module.getReceptor().getOutComm().terminateModule(module.getHandle());
        }
    }
    
    public void actionPerformed(ActionEvent e){
        Object o = e.getSource();

        if(o == detach) {
            detach();
        } else if(o == start){
            startAnalysis();
        }
    }
    
    private void startAnalysis() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Set selectedDb = module.getDatabases();
        String[] dbs = new String[selectedDb.size()];
        
        module.getArguments(new DataOutputStream(bout));
        
        Iterator it = selectedDb.iterator();
        DatabaseDescriptor db = null;
        for(int i = 0; it.hasNext(); i++) {
            db = (DatabaseDescriptor)it.next();
            dbs[i] = db.getName();
        }
        
        module.getReceptor().getOutComm().startAnalysis(module.getHandle(), dbs, bout.toByteArray());
    }
    
    private void localAddControls() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        topPanel.add(new DatabaseTable(this));
        topPanel.add(start = new JButton("Start Analysis"), BorderLayout.EAST);

        reportPanel.add(module.getResultsComponent());
        setupPanel.add(topPanel, BorderLayout.NORTH);
        setupPanel.add(module.getArgumentsComponent());
        
        start.addActionListener(this);
    }
    
    protected void addControls(){
        JTabbedPane tabs = new JTabbedPane();
        JToolBar toolBar = new JToolBar();
        
        Container c = getContentPane();
        
        c.setLayout(new BorderLayout());
        
        toolBar.add(detach = makeButton("", "Opens the module in a seperate window", IconResource.getFlowModuleDetach()));
        
        tabs.add("Report", reportPanel = new JPanel(new BorderLayout()));
        tabs.add("Setup", setupPanel = new JPanel(new BorderLayout()));;
        
        c.add(toolBar, BorderLayout.NORTH);
        c.add(tabs);
        
        setFrameIcon(IconResource.getAnlzModuleWindow());
        
        detach.addActionListener(this);
        toolBar.setBorder(toolBarBorder);
        toolBar.setBorderPainted(true);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
    }

    public void setButtonMask(int mask) {
    }
    
    private static class DatabaseTable extends JPanel implements TableModel, ReceptorStateListener, AnlzFrontendListener {
        private static final Class[] colClasses = new Class[] {String.class, Boolean.class, Boolean.class};
        private static final String[] colNames = new String[] {"Name", "Use", "Connected"};
        private static final Dimension size = new Dimension(0, 50);
        
        private JTable table;
        private List databases;
        private AnalysisPortal portal;
        private TableModelEvent event;
        
        public DatabaseTable(AnalysisPortal portal) {
            this.portal = portal;
            
            databases = new ArrayList();
            setLayout(new BorderLayout());
            
            add(new JScrollPane(table = new JTable(this)));
            
            event = new TableModelEvent(this);
            portal.module.getReceptor().getState().addReceptorStateListener(this);
            portal.module.addAnlzListener(this);
            
            this.setPreferredSize(size);
            receptorStateChanged();
        }
        
        public int getRowCount() {
            return databases.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int c) {
            return colNames[c];
        }

        public Class getColumnClass(int c) {
            return colClasses[c];
        }

        public boolean isCellEditable(int r, int c) {
            return c != 0;
        }

        public Object getValueAt(int r, int c) {
            DatabaseDescriptor db = (DatabaseDescriptor)databases.get(r);
            switch(c) {
                case 0: return db.getName();
                case 1: return Boolean.valueOf(portal.module.containsDatabase(db));
                case 2: return db.isConnected();
            }
            
            return null;
        }

        public void setValueAt(Object v, int r, int c) {
            if(v instanceof Boolean){
                Boolean bool = (Boolean)v;
                DatabaseDescriptor db = (DatabaseDescriptor)databases.get(r);
                
                switch(c) {
                    case 1: {
                        if(bool.booleanValue()) {
                            portal.module.addDatabase(db);
                        } else {
                            portal.module.removeDatabase(db);
                        }
                        break;
                    }
                    case 2: {
                        logger.info("Connecting DB: " + db);
                        portal.module.getReceptor().getOutComm().connectDatabase(db, bool.booleanValue());
                        break;
                    }
                }
            }
        }

        public void addTableModelListener(TableModelListener l) {
        }

        public void removeTableModelListener(TableModelListener l) {
        }

        public void receptorStateChanged() {
            //databases = (DatabaseDescriptor[])portal.module.getReceptor().getState().getDatabases().toArray(databases);
        }

        public void databaseListUpdated() {
            table.tableChanged(event);
        }

        public void databaseAdded(AddDatabaseEvent e) {
            databases.add(e.getDatabase());
            table.tableChanged(event);
        }

        public void databaseRemoved(RemoveDatabaseEvent e) {
            databases.remove(e.getDatabase());
            table.tableChanged(event);
        }

        public void databaseTypeAdded(AddDatabaseTypeEvent e) {
        }

        public void moduleAdded(AddModuleEvent e) {
        }

        public void streamServerAdded(AddStreamServerEvent e) {
        }

        public void streamServerRemoved(RemoveStreamServerEvent e) {
        }

        public void streamStatusChanged(StreamStatusChangedEvent e) {
        }

        public void receptorUserAdded(ReceptorUserAddedEvent e) {
        }

        public void receptorUserRemoved(ReceptorUserRemovedEvent e) {
        }
    }
}