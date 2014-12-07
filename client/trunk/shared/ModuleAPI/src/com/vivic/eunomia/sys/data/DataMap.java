/*
 * DataMap.java
 *
 * Created on November 24, 2007, 3:58 PM
 *
 */

package com.vivic.eunomia.sys.data;

/**
 * <p> This class represents a simple interface to a BDB Access object. The instance
 * is managed by the Sieve and it is not directly accessible through the Console. To
 * obtain this mapping use the DataManager: <br>
 * 
 * <pre>
 *    SieveContext.getDataManager().getDBMap("[Map Name]");
 * </pre>
 * 
 * <p> The maps will be stored on persistant storage by the Sieve. On first access
 * the data object will be created and will be accessible by that name from then on.
 * @author Mikhail Sosonkin
 */
public interface DataMap {
    /**
     * This method enables/disables data synchronization. Methods get and put are by 
     * default not synchronized and the data is passed to the underlying implementation.
     * @param sync Syncronization Flag
     */
    public void setSynchronized(boolean sync);
    
    /**
     * Returns the object performs the actual storage of data. At the moment only BDB
     * is used, so the Database is returned.
     * @return Underlying implementation object.
     */
    public Object getDataAccessObject(Object param);
    
    public boolean put(byte[] key, byte[] data);
    public byte[] get(byte[] key);
}