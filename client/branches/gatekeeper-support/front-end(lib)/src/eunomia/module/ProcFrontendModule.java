/*
 * GUIPlugin.java
 *
 * Created on January 31, 2006, 10:49 PM
 *
 */

package eunomia.module;

import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.frontend.GUIModule;
import eunomia.core.receptor.*;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.module.frontend.GUIPluginListener;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class ProcFrontendModule extends FrontendModule implements GUIModule {
    private GUIModule procmod;
    private Set listeners;
    private Set streams;
    private String name;
    private Receptor rec;
    
    public ProcFrontendModule(ModuleHandle handle, EunomiaModule mod, Receptor receptor) {
        super(handle, mod, receptor);
        
        if(mod instanceof GUIModule) {
            procmod = (GUIModule)module;
            procmod.setReceptor(receptor);
            listeners = new HashSet();
            streams = new HashSet();
        } else {
            throw new UnsupportedOperationException("ProcFrontendModule can only work with GUIModule");
        }
    }
    
    public void addStream(StreamServerDesc desc){
        streams.add(desc);
    }
    
    public void removeStream(StreamServerDesc desc) {
        streams.remove(desc);
    }
    
    public void resetStreamList() {
        streams.clear();
    }
    
    public Set getStreamsSet() {
        return streams;
    }
    
    public String getName(){
        return handle.getModuleName();
    }
    
    public void addGUIPluginListener(GUIPluginListener l){
        listeners.add(l);
    }
    
    public void removeGUIPluginListener(GUIPluginListener l){
        listeners.remove(l);
    }
    
    private void fireStatusUpdated(){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((GUIPluginListener)it.next()).statusUpdated(this);
        }
    }
    
    private void fireControlUpdated(){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((GUIPluginListener)it.next()).controlUpdated(this);
        }
    }

    private void fireControlObtained(){
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((GUIPluginListener)it.next()).controlObtained(this);
        }
    }
    
    public void fireStreamListUpdated() {
        Iterator it = listeners.iterator();
        
        while(it.hasNext()){
            ((GUIPluginListener)it.next()).streamListUpdated(this);
        }
    }
    
    //part getters.
    public JComponent getJComponent(){
        return procmod.getJComponent();
    }
    public JComponent getControlComponent(){
        return procmod.getControlComponent();
    }
    public String getTitle(){
        return procmod.getTitle();
    }

    public void processMessage(DataInputStream din) throws IOException {
        procmod.processMessage(din);
    }
    
    //properties
    public void setProperty(String name, Object value){
        procmod.setProperty(name, value);
    }
    public Object getProperty(String name){
        return procmod.getProperty(name);
    }
    
    //Communications
    public void updateStatus(InputStream in) throws IOException {
        procmod.updateStatus(in);
        this.fireStatusUpdated();
    }
    public void getControlData(OutputStream out) throws IOException {
        procmod.getControlData(out);
        this.fireControlObtained();
    }
    public void setControlData(InputStream in) throws IOException {
        procmod.setControlData(in);
        this.fireControlUpdated();
    }

    public void setReceptor(ConsoleReceptor receptor) {
        procmod.setReceptor(receptor);
    }
}