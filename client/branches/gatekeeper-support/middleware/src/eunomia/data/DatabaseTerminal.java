/*
 * DatabaseTerminal.java
 *
 * Created on April 10, 2006, 10:36 PM
 *
 */

package eunomia.data;

import eunomia.comm.interfaces.ErrorMessenger;
import eunomia.messages.receptor.msg.rsp.DatabaseQueryResultSetMessage;
import eunomia.util.database.sql.DiskResultSet;
import eunomia.util.io.StreamPipe;
import java.io.*;
import java.sql.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseTerminal implements Runnable {
    private ResultSet resultSet;
    private DiskResultSet dResultSet;
    private Database database;
    private String query;
    private ErrorMessenger err;
    
    public void setErrorMessenger(ErrorMessenger em){
        err = em;
    }

    public DatabaseQueryResultSetMessage sendQueryResult(Database db, String qry) {
        database = db;
        query = qry;
        
        DatabaseQueryResultSetMessage resp = new DatabaseQueryResultSetMessage();
        
        try {
            Statement stmt = database.getNewStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            stmt.execute(query);
            resultSet = stmt.getResultSet();

            StreamPipe index = new StreamPipe();
            StreamPipe result = new StreamPipe();

            resp.setIndex(index);
            resp.setResult(result);

            dResultSet = new DiskResultSet();
            dResultSet.setOutputStreams(result.getOutput(), index.getOutput());
            
            new Thread(this).start();
        } catch (Exception e){
            err("Error Querying (" + database + "): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        return resp;
    }
    
    public void run() {
        try {
            dResultSet.retrieveResultSet(resultSet);
        } catch (Exception e){
            err("Error Querying (" + database + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void err(String str){
        if(err != null){
            err.error(this, str);
        }
    }
}