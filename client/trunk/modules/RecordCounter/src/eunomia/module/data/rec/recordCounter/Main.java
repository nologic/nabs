/*
 * Main.java
 *
 * Created on November 21, 2006, 9:32 PM
 *
 */

package eunomia.module.data.rec.recordCounter;

import eunomia.data.Database;
import eunomia.messages.Message;
import eunomia.plugin.interfaces.StaticAnalysisModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Mikhail Sosonkin
 */
public class Main implements StaticAnalysisModule {
    private Message arguments;
    private Database db;
    private double progress;
    private int curRow;
    private int randNum;
    private StringBuilder resBuilder;
    
    public Main() {
        resBuilder = new StringBuilder();
    }

    public double getProgress() {
        return progress;
    }
    
    private ResultSet executeSQL() throws SQLException{
        return db.executeQuery("select count(*) from flows");
    }

    public void beginAnalysis() {
        try {
            curRow = 0;
            progress = 0.01;
            ResultSet set = executeSQL();
            while(set.next()){
                ++curRow;
                
                resBuilder.append(set.getInt(1) + "\n");
                
                progress = (double)randNum/(double)curRow;
            }
            progress = 1.0;
        } catch (Exception ex) {
            progress = 1.0;
            ex.printStackTrace();
        }
    }

    public void getArguments(DataOutputStream dout) {
        try {
            dout.writeInt(randNum);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getResult(DataOutputStream dout) {
        try {
            dout.write(resBuilder.toString().getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
    }

    public void setAndCheckParameters(DataInputStream in, List dbs) throws Exception {
        this.db = (Database)dbs.get(0);
    }
}