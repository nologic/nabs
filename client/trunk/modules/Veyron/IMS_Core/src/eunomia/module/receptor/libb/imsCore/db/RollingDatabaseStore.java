/*
 * RollingDatabaseStore.java
 *
 * Created on June 8, 2008, 11:01 AM
 *
 */

package eunomia.module.receptor.libb.imsCore.db;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface RollingDatabaseStore extends DatabaseStore {
    public void roll(String prefix) throws Exception;
}