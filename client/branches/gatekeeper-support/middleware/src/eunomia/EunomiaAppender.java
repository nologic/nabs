/*
 * EunomiaAppender.java
 *
 * Created on November 27, 2006, 6:52 PM
 *
 */

package eunomia;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Mikhail Sosonkin
 */
public class EunomiaAppender extends WriterAppender {
    private Set appenders;
    
    public EunomiaAppender(Layout layout, OutputStream os) {
        super(layout, os);
        
        appenders = new HashSet();
    }
    
    public void addEventer(EunomiaEventer ap){
        appenders.add(ap);
    }
    
    public void removeEventer(EunomiaEventer ap){
        appenders.remove(ap);
    }
    
    public void append(LoggingEvent event){
        super.append(event);
        
        if(appenders.size() != 0){
            Iterator it = appenders.iterator();
            while (it.hasNext()) {
                EunomiaEventer ap = (EunomiaEventer) it.next();
                ap.logEvent(event);
            }
        }
    }
}