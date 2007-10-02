
package org.cip4.JDFUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.lf5.util.StreamUtils;

/**
 *
 * @author  rainer
 *
 *
 * @web:servlet-init-param	name="" 
 *									value=""
 *									description=""
 *
 * @web:servlet-mapping url-pattern="/FixJDFServlet"
 */
public class DumpJDFServlet extends HttpServlet {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = -8902151736245089036L;
    
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
//      foo		
    }
    
    
    /** 
     * Handles all HTTP <code>GET / POST etc.</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
    {
        System.out.println("dump service");
        
        File f=JDFServletUtil.getTmpFile("dump", (String)null, "d", ".txt");
        
        try
        {
        FileOutputStream fs=new FileOutputStream(f);
        PrintWriter w=new PrintWriter(fs);
//        w.println("Context Type:"+http);
        w.println("Context Path:"+request.getContextPath());
        w.println("Context Type:"+request.getContentType());
        w.println("-------------------------------------------------");
        w.flush();
        StreamUtils.copyThenClose(request.getInputStream(), fs);
        OutputStream os=response.getOutputStream();
        w=new PrintWriter(os);
        w.print("<HTML><HEAD><TITLE>JDF Test DUMP</TITLE></HEAD>");
        w.print("<H1>Request Dump</H1><Body>");
        w.println("Context Path:"+request.getContextPath()+"<BR/>");
        w.println("Context Type:"+request.getContentType()+"<BR/>");
        w.print("</Body></HTML>");
        w.flush();
       }
        catch (Exception e) {
            System.out.println("dump service - snafu: "+e);
       }
         
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "DumpJDF Servlet";
    }
    
}
