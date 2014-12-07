/*
 * Login.java
 *
 * Created on October 28, 2007, 12:06 AM
 */

package eunomia.web;

import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.messages.receptor.ModuleHandle;
import com.vivic.eunomia.sys.util.Util;
import java.io.*;
import java.util.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Mikhail Sosonkin
 * @version
 */
public class Login extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();



        String sieve = request.getParameter("sieve");
        String user = request.getParameter("user");
        String pass = request.getParameter("pass");
        
        Receptor rec = ReceptorManager.v().getByName(sieve);
        
        if(rec != null) {
            try {
                if(!rec.isAuthenticated()) {
                    rec.setCredentials(user, pass);
                    rec.connect();

                    while(!rec.isAuthenticated()) {
                        Util.threadSleep(20);
                    }
                    rec.getOutComm().updateReceptor();
                    rec.getOutComm().getModuleList();
                    
                    Util.threadSleep(1000);
                    
                    session.setAttribute("sieve", sieve);
                    
                    out.println("Logged in, redirecting in 3 seconds<br>");
                    out.println("<a href=\"/RemoteGate/RealtimePanel\">Click here</a> if it doesn't");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            out.println("Seive '" + sieve + "' not found <br>");
        }
        
        out.println("<html>");
        out.println("<head>");
        
        out.println("<script type=\"text/javascript\">");
        out.println("<!--");
        out.println("function delayer(){");
        out.println("window.location = \"/RemoteGate/RealtimePanel\"");
        out.println("}");
        out.println("//-->");
        out.println("</script>");
        
        out.println("<title>Login Result Page</title>");
        out.println("</head>");
        out.println("<body onLoad=\"setTimeout('delayer()', 3000)\">");
        out.println("</body>");
        out.println("</html>");

        out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
