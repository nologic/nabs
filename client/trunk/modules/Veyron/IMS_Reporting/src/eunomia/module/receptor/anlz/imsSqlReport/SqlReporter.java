/*
 * SqlReporter.java
 *
 * Created on April 24, 2008, 12:26 AM
 *
 */

package eunomia.module.receptor.anlz.imsSqlReport;

import eunomia.module.receptor.libb.imsCore.Reporter;
import eunomia.module.receptor.libb.imsCore.net.NetworkChannel;

/**
 *
 * @author Mikhail Sosonkin
 */
public class SqlReporter implements Reporter {
    
    public SqlReporter() {
    }

    public void commandAndControlChannel(NetworkChannel channel) {
        System.out.println("CC: " + channel);
    }

    public void executeSql(String sql) {
    }
}