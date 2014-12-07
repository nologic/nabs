package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class _401_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.AnnotationProcessor _jsp_annotationprocessor;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
  }

  public void _jspDestroy() {
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;


  response.setHeader("WWW-Authenticate", "Basic realm=\"Tomcat Manager Application\"");

      out.write("\r\n");
      out.write("<html>\r\n");
      out.write(" <head>\r\n");
      out.write("  <title>401 Unauthorized</title>\r\n");
      out.write("  <style>\r\n");
      out.write("    <!--\r\n");
      out.write("    BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;font-size:12px;}\r\n");
      out.write("    H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;}\r\n");
      out.write("    PRE, TT {border: 1px dotted #525D76}\r\n");
      out.write("    A {color : black;}A.name {color : black;}\r\n");
      out.write("    -->\r\n");
      out.write("  </style>\r\n");
      out.write(" </head>\r\n");
      out.write(" <body>\r\n");
      out.write("   <h1>401 Unauthorized</h1>\r\n");
      out.write("   <p>\r\n");
      out.write("    You are not authorized to view this page. If you have not changed\r\n");
      out.write("    any configuration files, please examine the file\r\n");
      out.write("    <tt>conf/tomcat-users.xml</tt> in your installation. That\r\n");
      out.write("    file will contain the credentials to let you use this webapp.\r\n");
      out.write("   </p>\r\n");
      out.write("   <p>\r\n");
      out.write("    You will need to add <tt>manager</tt> role to the config file listed above.\r\n");
      out.write("    For example:\r\n");
      out.write("<pre>\r\n");
      out.write("&lt;role rolename=\"manager\"/&gt;\r\n");
      out.write("&lt;user username=\"tomcat\" password=\"s3cret\" roles=\"manager\"/&gt;\r\n");
      out.write("</pre>\r\n");
      out.write("   </p>\r\n");
      out.write("   <p>\r\n");
      out.write("    For more information - please see the\r\n");
      out.write("    <a href=\"/docs/manager-howto.html\">Manager App HOW-TO</a>.\r\n");
      out.write("   </p>\r\n");
      out.write(" </body>\r\n");
      out.write("\r\n");
      out.write("</html>\r\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try { out.clearBuffer(); } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
