/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eunomia.module.receptor.anlz.DNSCollectTest;

import com.sleepycat.collections.StoredIterator;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredValueSet;
import com.vivic.eunomia.module.Descriptor;
import com.vivic.eunomia.module.EunomiaModule;
import com.vivic.eunomia.module.receptor.ReceptorAnalysisModule;
import com.vivic.eunomia.sys.receptor.SieveContext;
import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.NetworkTopology;
import eunomia.module.receptor.libb.imsCore.VeyronAnalysisComponent;
import eunomia.module.receptor.libb.imsCore.dns.DNS;
import eunomia.module.receptor.libb.imsCore.dns.DNSMapping;
import eunomia.module.receptor.libb.imsCore.dns.DNSQuery;
import eunomia.module.receptor.libb.imsCore.dns.DNSResponse;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author justin
 */
public class Main implements VeyronAnalysisComponent, ReceptorAnalysisModule {
    DNS dns;
    
    public Main() {
        EunomiaModule mod = null;
        try {
            mod = SieveContext.getModuleManager().getInstanceEnsure("bootIms", Descriptor.TYPE_ANLZ);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        eunomia.module.receptor.anlz.bootIms.Main main = (eunomia.module.receptor.anlz.bootIms.Main)SieveContext.getModuleManager().unwrap(mod);
        
        main.registerAnalysisComponent(this, 10000, 1000);
    }

    public void initialize(NetworkTopology net, NetworkSymbols syms) {
        if (syms instanceof DNS) {
            dns = (DNS) syms;
        }
    }

    public void executeAnalysis() {
        System.out.println("==== Exectuting Analysis ====");
        
        DNSMapping mapping;
        StoredMap tmpMap = dns.getMappingMap();
        StoredValueSet values = (StoredValueSet) tmpMap.values();
        StoredIterator it = values.storedIterator();
        System.out.println("**** Mappings ****");
        while (it.hasNext()) {
            mapping = (DNSMapping) it.next();
            
            System.out.println("--------");
            System.out.print(mapping.toString());
        }
        it.close();
        
        DNSQuery query;
        
        tmpMap = dns.getQueryMap();
        values = (StoredValueSet) tmpMap.values();
        it = values.storedIterator();
        System.out.println("**** Queries ****");
        while (it.hasNext()) {
            query = (DNSQuery) it.next();
            
            System.out.println("--------");
            System.out.print(query.toString());
        }
        it.close();
        
        DNSResponse response;
        
        tmpMap = dns.getResponseMap();
        values = (StoredValueSet) tmpMap.values();
        it = values.storedIterator();
        System.out.println("**** Responses ****");
        while (it.hasNext()) {
            response = (DNSResponse) it.next();
            
            System.out.println("--------");
            System.out.print(response.toString());
        }
        it.close();
    }

    public void destroy() {
    }

    public void updateStatus(OutputStream out) throws IOException {
    }

    public void setControlData(InputStream in) throws IOException {
    }

    public void getControlData(OutputStream out) throws IOException {
    }

    public void processMessage(DataInputStream in, DataOutputStream out) throws IOException {
    }

    public void setProperty(String name, Object value) {
    }

    public Object getProperty(String name) {
        return null;
    }

    public Object[] getCommands() {
        return null;
    }

    public Object executeCommand(Object command, Object[] parameters) {
        return null;
    }

    public void threadMain() {
    }
}
