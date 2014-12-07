/*
 * NetworkNames.java
 *
 * Created on February 24, 2008, 3:50 PM
 *
 */

package eunomia.module.receptor.libb.imsCore;

import eunomia.module.receptor.libb.imsCore.dns.DNSFlowRecordFilter;
import java.util.Iterator;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface NetworkSymbols {
    public Iterator getDNSRecords(DNSFlowRecordFilter filter);
}