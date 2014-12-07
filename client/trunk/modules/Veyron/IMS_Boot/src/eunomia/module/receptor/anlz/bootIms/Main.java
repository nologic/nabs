/*
 * Main.java
 *
 * Created on December 27, 2007, 9:28 PM
 *
 */

package eunomia.module.receptor.anlz.bootIms;

import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import com.vivic.eunomia.sys.receptor.SieveModuleManager;
import com.vivic.eunomia.sys.receptor.SieveModuleServices;
import com.vivic.eunomia.sys.util.Util;
import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.db.NetEnv;
import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import eunomia.module.receptor.libb.imsCore.VeyronProcessingComponent;
import eunomia.module.receptor.libb.imsCore.db.DnsEnv;
import eunomia.module.receptor.libb.imsCore.db.NetworkInserter;
import eunomia.module.receptor.libb.imsCore.dns.DNS;
import eunomia.module.receptor.libb.imsCore.net.Network;
import eunomia.module.receptor.libb.imsCore.util.ComponentRegistry;
import eunomia.module.receptor.libb.imsCore.util.ComponentRegistryListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements ReceptorAnalysisModule, ComponentRegistryListener {
    private NetEnv imsEnv;
    private Network net;
    private NetworkInserter nins;
    private DnsEnv dnsEnv;
    private DNS dns;
    private boolean term;
    private eunomia.module.receptor.proc.netCollect.Main dsMain;
    private eunomia.module.receptor.proc.dnsCollect.Main dcMain;
    private TaskScheduler ts;
    private StatisticsComponent sComp;
    private Thread anlThread;
    private volatile AtomicBoolean netOpen;
    private AnalysisReporter reporters;
    private SampleComponent sampleComp;
    
    public Main() {
        reporters = new AnalysisReporter();
        netOpen = new AtomicBoolean(false);
        term = false;
        ts = new TaskScheduler();
        
        ComponentRegistry.getInstance().addComponentRegistryListener(this);
    }
    
    private void ensureNetOpen() {
        while(!netOpen.get()) {
            Util.threadSleep(20);
        }
    }

    public void destroy() {
        term = true;
        dsMain.stop();
        dcMain.stop();
        
        ensureNetOpen();
        
        Util.threadSleep(500);
        
        nins.close();
        dns.close();
    }

    public void updateStatus(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        
        if(dsMain == null) {
            dout.writeInt(0);
            dout.writeDouble(0.0);
        } else {
            dout.writeInt((int)dsMain.getBackLog());
            dout.writeDouble(dsMain.getCommitRate());
        }

        dout.writeInt(sampleComp.getHosts());
        dout.writeInt(sampleComp.getChans());
        dout.writeInt(sampleComp.getTraveralTime());
        dout.writeDouble(sampleComp.getTraveralRate());
        dout.writeDouble(sampleComp.getAvgCon());
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void setProperty(String name, Object value) {
        name = name.toLowerCase();
        
        ensureNetOpen();
        
        if(name.equals("net")) {
            String[] mask = value.toString().split("/");
            if(mask.length != 2) {
                return;
            }
            
            long ip = Util.getLongIp(mask[0]);
            int shift = Integer.parseInt(mask[1]);
            
            net.getNetworkDefinition().addSubnet(ip, shift);
        } else if(name.equals("stat")) {
            if(sComp == null) {
                try {
                    sComp = new StatisticsComponent(imsEnv, value.toString());
                    registerAnalysisComponent(sComp, 10000, 1000);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        } else if(name.equals("stat.fast")) {
            sComp.setFast(Boolean.valueOf(value.toString()));
        } else if(name.equals("stat.clear")) {
            sComp.setClear(Boolean.valueOf(value.toString()));
        } else if(name.equals("reg")) {
            System.out.println("Let's not use this anymore!");
            Thread.dumpStack();
            
            if(value instanceof VeyronProcessingComponent) {
                registerProcessingComponent((VeyronProcessingComponent)value);
            } else if(value instanceof Reporter) {
                reporters.addReporter((Reporter)value);
            }
        }
    }

    public Object getProperty(String name) {
        if(name.toLowerCase().equals("net")) {
            return net.getNetworkDefinition();
        }
        
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public void threadMain() {
        anlThread = Thread.currentThread();
        
        try {
            imsEnv = new NetEnv("imsBoot");
            nins = new NetworkInserter(imsEnv);
            net = new Network(imsEnv, nins);
            
            dnsEnv = new DnsEnv("imsBoot");
            dns = new DNS(dnsEnv);
            
            // Now opened.
            while(!netOpen.compareAndSet(false, true));

            SieveModuleManager modMan = SieveContext.getModuleManager();
            SieveModuleServices modServ = SieveContext.getModuleServices();

            // Collection module.
            //EunomiaModule dsMod = modMan.getInstanceEnsure("netCollect", Descriptor.TYPE_PROC);
            //dsMain = (eunomia.module.receptor.proc.netCollect.Main)modMan.unwrap(dsMod);
            //modServ.addDefaultConnect(modServ.getModuleHandle(dsMod));
            
            // DNS collection module.
            //EunomiaModule dcMod = modMan.getInstanceEnsure("dnsCollect", Descriptor.TYPE_PROC);
            //dcMain = (eunomia.module.receptor.proc.dnsCollect.Main)modMan.unwrap(dcMod);
            
            //registerProcessingComponent(dcMain);
            //modServ.addDefaultConnect(modServ.getModuleHandle(dcMod));
            
            registerAnalysisComponent(sampleComp = new SampleComponent(), 30000, 1000);
            
            while(!term) {
                Util.threadSleep(1000);
                
                ts.runTasks();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void registerAnalysisComponent(VeyronAnalysisComponent comp, long secInterval, long firstRunInterval) {
        ensureNetOpen();
        
        comp.initialize(net, dns);
        comp.setReporter(reporters);

        ts.addComponent(comp, secInterval, firstRunInterval);
    }

    private void registerProcessingComponent(VeyronProcessingComponent comp) {
        ensureNetOpen();
        
        comp.initialize(net, dns);
        comp.setReporter(reporters);
    }

    public void processingComponentAdded(VeyronProcessingComponent comp) {
        if(comp instanceof eunomia.module.receptor.proc.netCollect.Main) {
            dsMain = (eunomia.module.receptor.proc.netCollect.Main)comp;
            
            dsMain.setNetworkInserter(nins);
        }
        
        registerProcessingComponent(comp);
    }

    public void analysisComponentAdded(ComponentRegistry.AnlComp anl) {
        registerAnalysisComponent(anl.comp, anl.secInterval, anl.firstRunInterval);
    }

    public void reporterComponentAdded(Reporter rep) {
        reporters.addReporter(rep);
    }
}
