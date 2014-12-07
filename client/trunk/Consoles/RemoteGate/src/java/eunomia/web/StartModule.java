/*
 * StartModule.java
 *
 * Created on October 28, 2007, 11:39 PM
 */

package eunomia.web;

import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Mikhail Sosonkin
 * @version
 */
public class StartModule extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String sieve = request.getParameter("sieve");
        String op = request.getParameter("mod");
        
        Receptor rec = ReceptorManager.v().getByName(sieve);
        rec.getOutComm().instantiateModule(op);
        
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet StartModule</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Module started: " + op + "</h1>");
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
