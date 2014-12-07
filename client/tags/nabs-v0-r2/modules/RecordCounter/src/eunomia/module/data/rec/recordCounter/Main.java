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
    
    private void deleteTemps() throws SQLException {
        db.execute("DELETE FROM top");
        db.execute("DELETE FROM counts");
    }
    
    private ResultSet executeSQL(int count) throws SQLException{
        System.out.println(db);
        db.execute("CREATE TEMPORARY TABLE top (num BIGINT UNSIGNED, ip BIGINT UNSIGNED)");
        db.execute("CREATE TEMPORARY TABLE counts (data_type TINYINT UNSIGNED, num BIGINT UNSIGNED, ip BIGINT UNSIGNED, amount BIGINT UNSIGNED)");
        db.execute("INSERT INTO top (num, ip) SELECT COUNT(src_ip) AS \"num\", src_ip FROM flows GROUP BY src_ip ORDER BY \"num\" DESC");
        db.execute("INSERT INTO counts SELECT type, COUNT(type) AS \"num\", src_ip, top.num AS amount FROM flows INNER JOIN top ON src_ip = ip GROUP BY type, src_ip ORDER BY top.num DESC");
        return db.executeQuery("SELECT num, type, INET_NTOA(ip) FROM counts LIMIT " + 0 + ", " + count);
    }

    public void beginAnalysis(DataInputStream in, Database db) {
        try {
            this.db = db;
            int randNum = in.readInt();
            
            curRow = 0;
            progress = 0.01;
            ResultSet set = executeSQL(randNum);
            while(set.next()){
                ++curRow;
                
                resBuilder.append(set.getDouble(1) + " " + set.getInt(2) + " " + set.getString(3) + "\n");
                
                progress = (double)randNum/(double)curRow;
            }
            deleteTemps();
            progress = 1.0;
        } catch (Exception ex) {
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
}
