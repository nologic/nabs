package eunomia.module.receptor.libb.imsCore.dns;

import eunomia.module.receptor.libb.imsCore.iterators.IteratorFilter;
import java.util.ArrayList;

/**
 *
 * @author Justin Stallard
 */
public class DNSFlowRecordFilter implements IteratorFilter {
    
    private DNSQueryFilter queryFilter;
    private ArrayList<DNSResponseFilter> responseFilters;
    
    public DNSFlowRecordFilter() {
        responseFilters = new ArrayList<DNSResponseFilter>();
    }
    
    // returns true if a record matches the queryFilter AND any of the response filters
    public boolean allow(Object o) {
        if (queryFilter != null && !queryFilter.allow(o)) {
            return false;
        }
        
        if (responseFilters.isEmpty()) {
            return true;
        }
        
        for (DNSResponseFilter f : responseFilters) {
            if (f.allow(o)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void setQueryFilter(DNSQueryFilter queryFilter) {
        this.queryFilter = queryFilter;
    }
    
    public void addResponseFilter(DNSResponseFilter responseFilter) {
        responseFilters.add(responseFilter);
    }
    
    public void clear() {
        this.queryFilter = null;
        responseFilters.clear();
    }
}
