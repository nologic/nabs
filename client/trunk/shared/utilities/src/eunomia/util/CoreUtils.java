/*
 * CoreUtils.java
 *
 * Created on December 26, 2007, 11:46 PM
 *
 */

package eunomia.util;

/**
 *
 * @author Mikhail Sosonkin
 */
public class CoreUtils {
    public static void threadSleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
    }
}