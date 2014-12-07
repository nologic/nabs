/*
 * DataManager.java
 *
 * Created on June 13, 2005, 12:14 PM
 */

package eunomia.core.managers;

import eunomia.core.charter.*;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class DataManager {
    public static final DataManager ins = new DataManager();
    
    private DataEventThread det;
    
    private DataManager() {
        det = new DataEventThread(800);
        new Thread(det).start();
    }
    
    public void registerWithUpdater(DataEventNotifier c){
        det.addCharter(c);
    }
    
    public void deregisterWithUpdater(DataEventNotifier c){
        det.removeCharter(c);
    }
}