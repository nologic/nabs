/*
 * StreamStatusBar.java
 *
 * Created on June 14, 2005, 8:23 PM
 */

package eunomia.plugin.gui.feedBack;

import com.vivic.eunomia.module.frontend.FrontendProcessorModule;
import com.vivic.eunomia.sys.frontend.ConsoleContext;
import com.vivic.eunomia.sys.frontend.ConsoleReceptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

/**
 *
 * @author  Mikhail Sosonkin
 */
public class Main extends JPanel implements FrontendProcessorModule {
    private static String URL_VIEW = "http://bridge.poly.edu/view.pl";
    private static String URL_POST = "http://bridge.poly.edu/post.pl";
    private static String URL_RAND = "http://bridge.poly.edu/rand.pl";
    
    private FrontGUI mainComponent;
    
    private int randNum;
    
    private static Logger logger;
    
    static {
        logger = Logger.getLogger(Main.class);
    }
    
    public Main() {
        randNum = -1;
        mainComponent = new FrontGUI(this);
    }
    
    //part getters.
    public JComponent getJComponent(){
        return mainComponent;
    }
    
    public JComponent getControlComponent(){
        return null;
    }
    
    public String getTitle(){
        return "Feedback/Questions";
    }
    
    //properties
    public void setProperty(String name, Object value) {
    }
    
    public Object getProperty(String name){
        return null;
    }
    
    //Communications
    public void updateStatus(InputStream in) throws IOException {
        int tmpNum = 
                (in.read() & 0xFF) << 24 |
                (in.read() & 0xFF) << 16 | 
                (in.read() & 0xFF) << 8 | 
                (in.read()& 0xFF);
        
        if(randNum == -1) {
            randNum = tmpNum;
        }
    }
    
    public void getControlData(OutputStream out) throws IOException {
    }
    
    public void setControlData(InputStream in) throws IOException {
    }
    
    public void processMessage(DataInputStream din) throws IOException {
    }
    
    //Specifics
    private void getNumber() throws MalformedURLException, IOException {
        URL url = new URL(URL_RAND);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();
        
        String str = new String(bytes);
        String[] split = str.split("=");
        int num = Integer.parseInt(split[1]);
        
        DataOutputStream out = ConsoleContext.getReceptor().getManager().openInterModuleStream(this);
        out.writeInt(0); // set num command;
        out.writeInt(num);
        out.close();
        
        randNum = num;
    }
    
    void doPost(String sig, String aff, String text) throws UnsupportedEncodingException, MalformedURLException, IOException {
        if(randNum == -1) {
            getNumber();
            logger.info("User Id: " + randNum);
        }
        
        //generate post
        StringBuilder cont = new StringBuilder();
        cont.append(new Date(System.currentTimeMillis()).toString());
        cont.append("\n\n");
        cont.append(text);
        cont.append("\n\nSignature: ");
        cont.append(sig);
        cont.append("\nAffiliation: ");
        cont.append(aff);
        cont.append("\n-------------------------------------------------------------------------");
        
        StringBuilder data = new StringBuilder();
        data.append(URLEncoder.encode("num", "UTF-8"));
        data.append("=");
        data.append(URLEncoder.encode("" + randNum, "UTF-8"));
        data.append("&");
        data.append(URLEncoder.encode("content", "UTF-8"));
        data.append("=");
        data.append(URLEncoder.encode(cont.toString(), "UTF-8"));
        
        // send post
        URL url = new URL(URL_POST);
        URLConnection con = url.openConnection();
        con.setDoOutput(true);
        
        OutputStream out = con.getOutputStream();
        out.write(data.toString().getBytes());
        out.flush();
        
        InputStream in = con.getInputStream();
        in.close();
    }
    
    String getList() throws MalformedURLException, IOException {
        if(randNum == -1) {
            return "";
        }
        
        URL url = new URL(URL_VIEW + "?num=" + randNum);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();
        
        return new String(bytes);
    }
}