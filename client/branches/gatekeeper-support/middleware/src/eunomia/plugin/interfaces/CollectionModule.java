/*
 * CollectionModule.java
 *
 * Created on November 20, 2006, 10:18 PM
 *
 */

package eunomia.plugin.interfaces;

import com.vivic.eunomia.module.EunomiaModule;
import eunomia.data.Database;
import com.vivic.eunomia.module.receptor.FlowProcessor;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface CollectionModule extends EunomiaModule {
    public void destroy();
    public FlowProcessor getFlowProcessor();
    public void setDatabase(Database db) throws Exception;
}