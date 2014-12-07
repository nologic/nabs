/*
 * ModulePortal.java
 *
 * Created on October 28, 2007, 4:06 PM
 */

package eunomia.web;

import com.vivic.eunomia.module.frontend.GUIModule;
import eunomia.core.managers.ReceptorManager;
import eunomia.core.receptor.Receptor;
import eunomia.messages.receptor.ModuleHandle;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import javax.imageio.ImageIO;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.swing.JComponent;

/**
 *
 * @author Mikhail Sosonkin
 * @version
 */
public class ModulePortal extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String sieve = request.getParameter("sieve");

        Receptor rec = ReceptorManager.v().getByName(sieve);
        ModuleHandle handle = new ModuleHandle();
        handle.setInstanceID(id);
        handle.setModuleType(ModuleHandle.TYPE_PROC);
        GUIModule mod = (GUIModule)rec.getManager().getModule(handle);
        
        Object webProp = mod.getProperty("web");

        if(webProp instanceof Image) {
            response.setContentType("image/jpeg");
            OutputStream out = response.getOutputStream();


            //JComponent com = mod.getJComponent();

            Image img = (Image)webProp;//new BufferedImage(800, 680, BufferedImage.TYPE_INT_RGB);
            /*Graphics g = img.getGraphics();
            com.setSize(800, 600);
            com.setVisible(true);
            com.paintComponents(g);*/
            ImageIO.write((RenderedImage)img, "jpg", out);
            out.close();
        } else {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Login</title>");
            out.println("</head>");
            out.println("<body><pre>");
            
            out.print(webProp);
            
            out.println("<pre></body>");
            out.println("</html>");

            out.close();
        }
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
