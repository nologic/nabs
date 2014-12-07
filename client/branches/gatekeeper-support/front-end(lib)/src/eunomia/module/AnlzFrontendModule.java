/*
 * AnlzFrontendModule.java
 *
 * Created on January 14, 2007, 3:27 PM
 *
 */

package eunomia.module;

import eunomia.core.receptor.Receptor;
import eunomia.messages.DatabaseDescriptor;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.frontend.GUIStaticAnalysisModule;
import com.vivic.eunomia.module.frontend.AnlzFrontendListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnlzFrontendModule extends FrontendModule implements GUIStaticAnalysisModule {
    private GUIStaticAnalysisModule anlmod;
    private List listeners;
    private Set databases;
    
    public AnlzFrontendModule(ModuleHandle handle, EunomiaModule mod, Receptor receptor) {
        super(handle, mod, receptor);
        
        listeners = new LinkedList();
        databases = new HashSet();
        
        if(mod instanceof GUIStaticAnalysisModule) {
            anlmod = (GUIStaticAnalysisModule)module;
        } else {
            throw new UnsupportedOperationException("AnlzFrontendModule can only work with GUIStaticAnalysisModule");
        }
    }
    
    public void addAnlzListener(AnlzFrontendListener l){
        listeners.add(l);
    }
    
    public void removeAnlzListener(AnlzFrontendListener l){
        listeners.remove(l);
    }
    
    public void fireDatabaseListUpdated() {
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            AnlzFrontendListener l = (AnlzFrontendListener) it.next();
            l.databaseListUpdated();
        }
    }
    
    public void addDatabase(DatabaseDescriptor db) {
        databases.add(db);
    }
    
    public void removeDatabase(DatabaseDescriptor db) {
        databases.remove(db);
    }
    
    public boolean containsDatabase(DatabaseDescriptor db) {
        return databases.contains(db);
    }
    
    public Set getDatabases() {
        return databases;
    }

    public void setArguments(DataInputStream in) {
        anlmod.setArguments(in);
    }

    public void getArguments(DataOutputStream out) {
        anlmod.getArguments(out);
    }

    public void setResult(DataInputStream in) {
        anlmod.setResult(in);
    }

    public JComponent getArgumentsComponent() {
        return anlmod.getArgumentsComponent();
    }

    public JComponent getResultsComponent() {
        return anlmod.getResultsComponent();
    }
}
