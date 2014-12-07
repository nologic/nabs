package eunomia.module.receptor.libb.imsCore.dns;

import com.sleepycat.db.DatabaseException;
import eunomia.module.receptor.libb.imsCore.NetworkSymbols;
import eunomia.module.receptor.libb.imsCore.db.DnsEnv;
import eunomia.module.receptor.libb.imsCore.iterators.FilteredMultiIterator;
import java.util.Iterator;

/**
 *
 * @author justin
 */
public class DNS implements NetworkSymbols {
    public static final int TIME_SHIFT    = 10; // 1024 seconds about 17.067 minutes.
    public static final int MAX_TIME_DISTANCE = 85; // 85 intervals is 24.178 hours.
    
    private DnsEnv env;
    
    public DNS(DnsEnv e) {
        this.env = e;
    }
    
    public void addRecords(DNSFlowRecord[] records, int count) {
        env.commit(records, DnsEnv.PRI_FLOWS, 0, count);
    }
    
    public void close() {
        try {
            env.close();
        } catch (DatabaseException ex) {
            ex.printStackTrace();
        }
    }
    
    public void rollDB() {
        env.rollDB(DnsEnv.PRI_FLOWS);
    }
    
    public Iterator getDNSRecords(DNSFlowRecordFilter filter) {
        if (filter == null) {
            return env.getValuesIterator(DnsEnv.PRI_FLOWS);
        } else {
            return new FilteredMultiIterator(env.getValuesIterator(DnsEnv.PRI_FLOWS), filter);
        }
    }
}