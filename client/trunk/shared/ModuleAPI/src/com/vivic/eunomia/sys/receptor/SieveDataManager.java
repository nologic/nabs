/*
 * SieveDataManager.java
 *
 * Created on November 24, 2007, 3:16 PM
 *
 */

package com.vivic.eunomia.sys.receptor;

import com.vivic.eunomia.sys.data.DataMap;
import java.util.Map;

/**
 *
 * @author Mikhail Sosonkin
 */
public interface SieveDataManager {
    public DataMap getDBMap(String name, Map paramMap) throws Exception;
}