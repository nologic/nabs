/*
 * HostResolver.java
 *
 * Created on August 24, 2005, 12:42 PM
 *
 */

package eunomia.util;

import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class HostResolver implements Runnable {
    private static final HostResolver ins = new HostResolver();
    
    private LinkedList request;
    
    private HostResolver() {
        request = new LinkedList();
        new Thread(this).start();
        new Thread(this).start();
        new Thread(this).start();
        new Thread(this).start();
    }
    
    public void run() {
        while(true){
            ResolveRequest rr = null;
            
            synchronized(request){
                if(request.size() > 0){
                    rr = (ResolveRequest)request.removeFirst();
                }
            }
            
            if(rr != null){
                InetAddress add = rr.getAddress();
                if(add != null){
                    rr.setResolved(add.getHostName());
                }
            } else {
                try {
                    Thread.sleep(20);
                } catch (Exception e){
                }
            }
        }
    }
    
    private void addRequestV(ResolveRequest rr){
        synchronized(request){
            if(!request.contains(rr)){
                request.addLast(rr);
            }
        }
    }
    
    public static void addRequest(ResolveRequest rr){
        ins.addRequestV(rr);
    }
}