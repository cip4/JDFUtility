
package org.cip4.JDFUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.log4j.lf5.util.StreamUtils;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.pool.JDFAuditPool;
import org.cip4.jdflib.resource.JDFModified;

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
    
    
    /** Handles the HTTP <code>GET</code> method.
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
        }
        catch (Exception e) {
            System.out.println("dump service - snafu"+e);
       }
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "DumpJDF Servlet";
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
    {
       //doService(arg0, arg1,"get");
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
    {
        //doService(arg0, arg1,"post");
    }
    
}
