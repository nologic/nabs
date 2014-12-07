/*
 * CollectionModule.java
 *
 * Created on November 20, 2006, 10:18 PM
 *
 */

package eunomia.plugin.interfaces;

import eunomia.data.Database;
import eunomia.flow.FlowProcessor;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface CollectionModule {
    public FlowProcessor getFlowProcessor();
    //public 
    
    // later to be changed to a DB module.
    public List getDatabases();
    public void addDatabase(Database db);
}