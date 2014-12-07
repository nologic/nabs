/*
 * AnlzFrontendModule.java
 *
 * Created on January 14, 2007, 3:27 PM
 *
 */

package eunomia.module;

import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.frontend.FrontendAnalysisModule;
import com.vivic.eunomia.module.frontend.FrontendAnalysisListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 */
public class AnlzFrontendModule extends FrontendModule implements FrontendAnalysisModule, ReportingFrontendModule {
    private FrontendAnalysisModule anlmod;
    private List listeners;
    
    public AnlzFrontendModule(ModuleHandle handle, EunomiaModule mod, Receptor receptor) {
        super(handle, mod, receptor);
        
        listeners = new LinkedList();
        
        if(mod instanceof FrontendAnalysisModule) {
            anlmod = (FrontendAnalysisModule)module;
        } else {
            throw new UnsupportedOperationException("AnlzFrontendModule can only work with FrontendAnalysisModule");
        }
    }
    
    public void addAnlzListener(FrontendAnalysisListener l){
        listeners.add(l);
    }
    
    public void removeAnlzListener(FrontendAnalysisListener l){
        listeners.remove(l);
    }

    public JComponent getJComponent() {
        return anlmod.getJComponent();
    }

    public JComponent getControlComponent() {
        return anlmod.getControlComponent();
    }

    public String getTitle() {
        return anlmod.getTitle();
    }

    public void processMessage(DataInputStream din) throws IOException {
        anlmod.processMessage(din);
    }

    public void setReceptor(ConsoleReceptor receptor) {
        anlmod.setReceptor(receptor);
    }

    public void setProperty(String name, Object value) {
        anlmod.setProperty(name, value);
    }

    public Object getProperty(String name) {
        return anlmod.getProperty(name);
    }

    public void updateStatus(InputStream in) throws IOException {
        anlmod.updateStatus(in);
    }

    public void getControlData(OutputStream out) throws IOException {
        anlmod.getControlData(out);
    }

    public void setControlData(InputStream in) throws IOException {
        anlmod.setControlData(in);
    }
}
