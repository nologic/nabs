/*
 * Main.java
 *
 * Created on October 27, 2007, 10:53 PM
 */

package eunomia.web;

import eunomia.config.Config;
import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import java.io.*;
import java.util.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 *
 * @author Mikhail Sosonkin
 * @version
 */
public class Main extends HttpServlet {
    private static boolean isLoaded = false;
    
    private void initialLoad() {
        if(!isLoaded) {
            Layout layout = new PatternLayout("%d{HH:mm:ss} %-5p: %m%n");
            WriterAppender wa = new WriterAppender(layout, System.out);
            BasicConfigurator.configure(wa);
        
            try {
                Config.setGlobalName("frontend");
            } catch (Exception e) {
                System.out.println("Unable to open configurations database: " + e.getMessage());
                System.exit(1);
            }
            ReceptorManager.v().load();
            isLoaded = true;
        }
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        initialLoad();
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>NABS Main web page</title>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<H1>Receptors:</H1>");
        out.println("<ol>");
        Iterator it = ReceptorManager.v().getReceptors().iterator();
        while (it.hasNext()) {
            Receptor r = (Receptor) it.next();
            
            out.println("<li><b>" + r + " (" + r.getIP() + ":" + r.getPort() + ") Logged in: " + r.isAuthenticated() + "</b><br>");
            
            if(r.isAuthenticated()) {
                out.println("<a href=\"RealtimePanel?sieve=" + r + "\">View Sieve</a>");
            } else {
                out.println("<form action=\"Login\" method=\"POST\">");
                out.println("Username: <input type=\"text\" name=\"user\" value=\"\" size=\"10\" />");
                out.println("Password: <input type=\"password\" name=\"pass\" value=\"\" size=\"10\" />");
                out.println("<input type=\"submit\" value=\"Login\" />");
                out.println("<input type=\"hidden\" name=\"sieve\" value=\"" + r + "\" />");
                out.println("</form>");
            }

            out.println("</li>");
        }
        
        out.println("</ol>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
    
    // <editor-fold desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
