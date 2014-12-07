/*
 * GUIPlugin.java
 *
 * Created on January 31, 2006, 10:49 PM
 *
 */

package eunomia.plugin;

import eunomia.core.receptor.*;
import eunomia.core.receptor.listeners.MessageReceiver;
import eunomia.plugin.interfaces.*;
import eunomia.flow.Filter;
import eunomia.messages.receptor.ModuleHandle;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class GUIPlugin implements GUIModule {
    private GUIModule mod;
    private Set listeners;
    private Set streams;
    private String name;
    private Receptor rec;
    private ModuleHandle handle;
    
    public GUIPlugin(String name, GUIModule mod) {
        this.mod = mod;
        this.name = name;
        listeners = new HashSet();
        streams = new HashSet();
    }
    
    public ModuleHandle getModuleHandle(){
        if(handle == null){
            handle = rec.getManager().getModuleHandle(this);
        }
        
        return handle;
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
        return name;
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
    
    public GUIModule getModule(){
        return mod;
    }
    
    //part getters.
    public JComponent getJComponent(){
        return mod.getJComponent();
    }
    public JComponent getControlComponent(){
        return mod.getControlComponent();
    }
    public String getTitle(){
        return mod.getTitle();
    }

    public MessageReceiver getReceiver(){
        return mod.getReceiver();
    }
    
    //module options
    public void setReceptor(Receptor receptor){
        rec = receptor;
        mod.setReceptor(receptor);
    }
    public Receptor getReceptor(){
        return rec;
    }
    
    //properties
    public void setProperty(String name, Object value){
        mod.setProperty(name, value);
    }
    public Object getProperty(String name){
        return mod.getProperty(name);
    }
    
    //Communications
    public void updateStatus(InputStream in) throws IOException {
        mod.updateStatus(in);
        this.fireStatusUpdated();
    }
    public void getControlData(OutputStream out) throws IOException {
        mod.getControlData(out);
        this.fireControlObtained();
    }
    public void setControlData(InputStream in) throws IOException {
        mod.setControlData(in);
        this.fireControlUpdated();
    }
}