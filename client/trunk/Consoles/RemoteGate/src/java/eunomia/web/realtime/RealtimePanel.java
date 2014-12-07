/*
 * RealtimePanel.java
 *
 * Created on November 4, 2007, 10:33 PM
 */

package eunomia.web.realtime;

import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import eunomia.core.receptor.StreamServerDesc;
import eunomia.messages.receptor.ModuleHandle;
import java.io.*;
import java.util.Iterator;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Mikhail Sosonkin
 * @version
 */
public class RealtimePanel extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Sieve View</title>");
        out.println("</head>");
        out.println("<body>");
        
        
        String sieve = (String)session.getAttribute("sieve");
        if(sieve == null) {
            sieve = request.getParameter("sieve");
            session.setAttribute("sieve", sieve);
        }
        
        Receptor rec = ReceptorManager.v().getByName(sieve);

        try { 
            out.println("<h2>Module Instances:</h2>");
            Iterator it = rec.getManager().getHandlesList().iterator();
            while (it.hasNext()) {
                ModuleHandle handle = (ModuleHandle) it.next();
                out.println("<a href=ModulePortal?sieve=" + sieve + "&id=" + handle.getInstanceID() + ">" + handle + "</a><br>");
            }

            out.println("<h2>Senors:</h2>");
            it = rec.getState().getStreamServers().iterator();
            while (it.hasNext()) {
                StreamServerDesc ss = (StreamServerDesc) it.next();
                out.println(ss + " connected: " + ss.isConnected());
                out.println("<a href=Sensor?sieve=" + sieve + "&sensor=" + ss.getName() + "&op=con>Connect</a>)");
                out.println("<a href=Sensor?sieve=" + sieve + "&sensor=" + ss.getName() + "&op=dis>Disconnect</a>)<br>");
            }

            out.println("<h2>Start Module:</h2>");
            it = rec.getState().getModules().iterator();
            while (it.hasNext()) {
                String mod = (String) it.next();
                out.println("(<a href=StartModule?sieve=" + sieve + "&mod=" + mod + ">" + mod + "</a><br>");
            }

        } catch (Exception e) {
            out.println("Error occured " + e.getMessage());
        }
        
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
