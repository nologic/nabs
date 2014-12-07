/*
 * PolicyChangeListener.java
 *
 * Created on January 4, 2007, 5:10 PM
 */

package eunomia.plugin.gui.networkPolicy;

import eunomia.plugin.com.networkPolicy.PolicyItem;

/**
 *
 * @author kulesh
 */
public interface PolicyListChangeListener {
    public void policyAdded(PolicyItem p);
    public void policyRemoved(PolicyItem p);
}