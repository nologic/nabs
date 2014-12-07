/*
 * DatabaseTerminal.java
 *
 * Created on April 10, 2006, 10:36 PM
 *
 */

package eunomia.data;

import eunomia.comm.interfaces.ErrorMessenger;
import eunomia.util.Util;
import eunomia.util.database.sql.DiskResultSet;
import java.io.*;
import java.net.*;
import java.sql.*;

/**
 *
 * @author Mikhail Sosonkin
 */
public class DatabaseTerminal implements Runnable {
    private int random1;
    private int random2;
    private ResultSet resultSet;
    private DiskResultSet dResultSet;
    private Database database;
    private String query;
    private String allowIp;
    private ServerSocket contentSockSrv;
    private ServerSocket indecesSockSrv;
    private ErrorMessenger err;
    
    public DatabaseTerminal(String remoteIp) throws IOException, Exception {
        random1 = Util.getRandomInt(null);
        random2 = Util.getRandomInt(null);
        allowIp = remoteIp;
        
        contentSockSrv = new ServerSocket(0);
        indecesSockSrv = new ServerSocket(0);
    }
    
    public void setErrorMessenger(ErrorMessenger em){
        err = em;
    }
    
    public int getRandom1(){
        return random1;
    }
    
    public int getRandom2(){
        return random2;
    }
    
    public int getPort1(){
        return contentSockSrv.getLocalPort();
    }
    
    public int getPort2(){
        return indecesSockSrv.getLocalPort();
    }

    public void sendQueryResult(Database db, String qry) throws IOException {
        if(query == null){
            database = db;
            query = qry;
            
            new Thread(this).start();
        }
    }
    
    private boolean checkSockets(Socket s1, Socket s2) throws IOException {
        DataInputStream din;
        String address;
        
        address = s1.getInetAddress().getHostAddress();
        if(!address.equals(allowIp)){
            return false;
        }
        
        address = s2.getInetAddress().getHostAddress();
        if(!address.equals(allowIp)){
            return false;
        }
        
        din = new DataInputStream(s1.getInputStream());
        if(din.readInt() != random1){
            return false;
        }
        din = new DataInputStream(s2.getInputStream());
        if(din.readInt() != random2){
            return false;
        }

        return true;
    }
    
    public void run() {
        Socket contentSock = null;
        Socket indecesSock = null;
        
        try {
            Statement stmt;

            dResultSet = new DiskResultSet();

            contentSock = contentSockSrv.accept();
            indecesSock = indecesSockSrv.accept();
            contentSockSrv.close();
            indecesSockSrv.close();

            if(!checkSockets(contentSock, indecesSock)){
                contentSock.close();
                indecesSock.close();
                err("Connection Token Check Failed (" + database + ")");
                return;
            }
            
            dResultSet.setOutputStreams(contentSock.getOutputStream(), indecesSock.getOutputStream());

            database.connect();
            stmt = database.getNewStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            stmt.execute(query);
            resultSet = stmt.getResultSet();
            dResultSet.retrieveResultSet(resultSet);
        } catch (Exception e){
            if(contentSock != null){
                try {
                    contentSock.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if(indecesSock != null){
                try {
                    indecesSock.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
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