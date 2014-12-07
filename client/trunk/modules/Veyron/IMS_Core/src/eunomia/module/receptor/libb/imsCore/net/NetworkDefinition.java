/*
 * NetworkDefinition.java
 *
 * Created on February 23, 2008, 5:30 PM
 *
 */

package eunomia.module.receptor.libb.imsCore.net;

import com.vivic.eunomia.sys.util.Util;

/**
 *
 * @author Mikhail Sosonkin
 */
public class NetworkDefinition {
    private long[] ips;
    private int[] shifts;
    
    public NetworkDefinition() {
        ips = new long[0];
        shifts = new int[0];
    }
    
    public void addSubnet(long ip, int shift) {
        int l = ips.length;
        
        long[] ipsTmp = new long[l + 1];
        int[] shiftsTmp = new int[l + 1];
        
        System.arraycopy(ipsTmp, 0, ips, 0, l);
        System.arraycopy(shiftsTmp, 0, shifts, 0, l);
        
        ipsTmp[l] = ip >> shift;
        shiftsTmp[l] = shift;
        
        ips = ipsTmp;
        shifts = shiftsTmp;
    }
    
    public void removeSubnet(long ip, int shift) {
        ip >>= shift;
        
        int found = -1;
        
        for (int i = 0; i < shifts.length; i++) {
            if(ips[i] == ip && shifts[i] == shift) {
                found = i;
            }
        }
        
        if(found > -1) {
            int l = ips.length;

            long[] ipsTmp = new long[l - 1];
            int[] shiftsTmp = new int[l - 1];
            
            for (int i = 0, k = 0; i < shifts.length; i++) {
                if(i != found) {
                    ipsTmp[k] = ips[i];
                    shiftsTmp[k] = shifts[i];
                    
                    k++;
                }
            }

            ips = ipsTmp;
            shifts = shiftsTmp;
        }
    }
    
    public boolean isInNetowrk(long ip) {
        if(shifts.length == 0) {
            // Network undefined.
            return true;
        }
        
        for (int i = 0; i < shifts.length; i++) {
            int sh = shifts[i];
         
            //System.out.println(Util.ipToString(ip) + " - " + Util.ipToString(ip >> sh));
            if(ip >> sh == ips[i]) {
                return true;
            }
        }
        
        return false;
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        for (int i = 0; i < shifts.length; i++) {
            Util.ipToString(b, ips[i] << shifts[i]).append("/").append(shifts[i]).append("\n");
        }
        
        return b.toString();
    }
}