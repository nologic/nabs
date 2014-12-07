/*
 * SqlEnvironmentKeyCreator.java
 *
 * Created on June 7, 2008, 5:51 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.creators;

import eunomia.module.receptor.libb.imsCore.EnvironmentEntry;
import eunomia.module.receptor.libb.imsCore.EnvironmentKey;
import java.sql.PreparedStatement;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface SqlEnvironmentKeyCreator {
    public String[] getColumnNames();
    public String[] getColumnTypes();
    public void getSqlFieldValue(EnvironmentEntry entry, PreparedStatement s, int f) throws Exception;
    public void getSqlFieldValue(Object entry, PreparedStatement s, int f) throws Exception;
}