/*
 * HostPair.java
 *
 * Created on March 22, 2007, 8:45 PM
 *
 */

package eunomia.plugin.rec.networkPolicy;

import eunomia.plugin.com.networkPolicy.PolicyItem;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HostPair {
    private Host source;
    private Host destination;
    
    public HostPair() {
    }

    public Host getSource() {
        return source;
    }

    public void setSource(Host source) {
        this.source = source;
    }

    public Host getDestination() {
        return destination;
    }

    public void setDestination(Host destination) {
        this.destination = destination;
    }
    
    public void accountGlobalData(int t, int size, long time) {
        source.accountGlobalData(t, size, time);
        destination.accountGlobalData(t, size, time);
    }
}