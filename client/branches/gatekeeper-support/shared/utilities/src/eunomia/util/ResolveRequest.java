/*
 * ResolveRequest.java
 *
 * Created on August 24, 2005, 12:43 PM
 *
 */

package eunomia.util;

import java.net.InetAddress;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface ResolveRequest {
    public InetAddress getAddress();
    public void setResolved(String hName);
}
