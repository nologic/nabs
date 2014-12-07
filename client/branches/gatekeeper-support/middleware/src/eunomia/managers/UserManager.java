/*
 * UserManager.java
 *
 * Created on February 21, 2006, 4:39 PM
 */

package eunomia.managers;

import eunomia.exception.ManagerException;
import eunomia.exception.UserChangeException;
import eunomia.util.Util;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 *
 * @author Mikhail Sosonkin
 */
public class UserManager {
    private static UserManager ins;
    private static Logger logger;
    
    private String passfile;
    private int count;
    
    private UserManager(String pf) {
        passfile = pf;
        try {
            count = 0;
            byte[] admin = getPassHash("root");
            if(admin == null){
                logger.info("Setting default root password to \'toor\'.");
                addUser("root", "toor");
            } else {
                doCount();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public int getUserCount() {
        return count;
    }
    
    public String[] getUsersList() {
        String[] user = new String[getUserCount()];
        int i = 0;
        
        try {
            FileReader fReader = new FileReader(passfile);
            BufferedReader reader = new BufferedReader(fReader);
            String line;
            while( (line = reader.readLine()) != null){
                line = line.trim();

                String[] namepass = line.split(":");
                if(namepass.length == 2){
                    user[i++] = namepass[0];
                }
            }
            reader.close();
            fReader.close();
        } catch (IOException e){
            //e.printStackTrace();
        }
        
        return user;
    }
    
    private void doCount() {
        try {
            FileReader fReader = new FileReader(passfile);
            BufferedReader reader = new BufferedReader(fReader);
            String line;
            while( (line = reader.readLine()) != null){
                count++;
            }
            reader.close();
            fReader.close();
        } catch (IOException e){
            //e.printStackTrace();
        }
    }
    
    public void addUser(String user, String pass) throws IOException, ManagerException {
        if(!isValidName(user)){
            throw new UserChangeException("Name has illegal characters");
        }
        
        byte[] md5;
        try {
            md5 = Util.md5(pass.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new ManagerException("Error in hash function: " + ex.getMessage());
        }
        
        String encPass = Util.hexEncode(md5);
        
        FileOutputStream fout = new FileOutputStream(passfile, true);
        fout.write(user.getBytes());
        fout.write((int)':');
        fout.write(encPass.getBytes());
        fout.write((int)'\n');
        fout.close();
        
        count++;
        logger.info("Added User: " + user);
    }
    
    public void removeUser(String user) throws ManagerException {
        if(getPassHash(user) == null)
            throw new UserChangeException("User '" + user + "' does not exit");
        
        try {
            StringBuilder newPass = new StringBuilder();
            
            FileReader fReader = new FileReader(passfile);
            BufferedReader reader = new BufferedReader(fReader);
            String line;
            while( (line = reader.readLine()) != null){
                if(line.indexOf(user) != -1){
                    line = line.trim();
                    
                    String[] namepass = line.split(":");
                    if(namepass.length == 2 && namepass[0].equals(user)){
                        logger.info("Removed user: " + user);
                        continue;
                    }
                }
                
                newPass.append(line);
                newPass.append("\n");
            }
            reader.close();
            fReader.close();
            
            FileOutputStream fout = new FileOutputStream(passfile);
            fout.write(newPass.toString().getBytes());
            fout.close();
        } catch (IOException e){
            //e.printStackTrace();
        }
    }
    
    public void changePassword(String username, String pass) throws IOException, ManagerException {
        // Need to test this.
        byte[] hash = null;
        String encPass = null;
        byte[] md5;
        long pos;
        
        try {
            md5 = Util.md5(pass.getBytes());
            encPass = Util.hexEncode(md5);
        } catch (NoSuchAlgorithmException ex) {
            throw new ManagerException("Error in hash function: " + ex.getMessage());
        }
        
        RandomAccessFile rand = new RandomAccessFile(passfile, "rw");
        pos = rand.getFilePointer();
        String line;
        while( (line = rand.readLine()) != null){
            if(line.indexOf(username) != -1){
                int index = line.indexOf(':');
                int size = encPass.length();
                
                rand.seek(pos + index + 1);
                rand.write(encPass.getBytes());
                break;
            }
            
            pos = rand.getFilePointer();
        }
        rand.close();
    }
    
    public boolean checkPass(String username, String pass) throws NoSuchAlgorithmException {
        byte[] hash = getPassHash(username);
        
        if(hash == null) {
            return false;
        }
        
        return Arrays.equals(hash, Util.md5(pass.getBytes()));
    }
    
    public byte[] getPassHash(String username) {
        byte[] hash = null;
        try {
            FileReader fReader = new FileReader(passfile);
            BufferedReader reader = new BufferedReader(fReader);
            String line;
            while( (line = reader.readLine()) != null){
                if(line.indexOf(username) != -1){
                    line = line.trim();
                    
                    String[] namepass = line.split(":");
                    if(namepass.length == 2 && namepass[0].equals(username)){
                        String pass = namepass[1];
                        try {
                            hash = Util.hexDecode(pass);
                            break;
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            reader.close();
            fReader.close();
        } catch (IOException e){
            //e.printStackTrace();
        }
        
        return hash;
    }
    
    public static void initialize(String passFile){
        if(ins == null){
            logger = Logger.getLogger(UserManager.class);
            ins = new UserManager(passFile);
        } else {
            throw new UnsupportedOperationException("User Manager already initialized");
        }
    }
    
    public static UserManager v(){
        if(ins == null){
            throw new UnsupportedOperationException("User Manager not initialized");
        }
        
        return ins;
    }
    
    public static boolean isValidName(String name){
        char[] chars = name.toCharArray();
        for(int i = chars.length - 1; i != -1; --i){
            switch(chars[i]){
                case ' ':
                case '\n':
                case '\t':
                case ':':
                    return false;
            }
        }
        
        return true;
    }
}