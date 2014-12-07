/*
 * PolicySelectionListener.java
 *
 * Created on June 19, 2007, 10:58 PM
 *
 */

package eunomia.plugin.gui.networkPolicy.tree;

import eunomia.plugin.com.networkPolicy.PolicyItem;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface PolicySelectionListener {
    public void policyItemSelection(PolicyItem item);
    public void policyTypeSelection(int type);
}
