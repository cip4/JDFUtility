
package org.cip4.JDFUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;

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
public class GetFileServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(GetFileServlet.class.getName());

    /**
     * 
     */
    private static final long serialVersionUID = -8902154436245089036L;
    private File baseDir=null;

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final String root = config.getInitParameter("rootDir");
        System.out.println("Config root: "+root);
        baseDir=new File(root);
        baseDir.mkdir(); // create if it aint there
    }

    /** Destroys the servlet.
     */
    public void destroy() {
//      foo		
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws IOException 
     * @throws ServletException 
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }


    /**
     * Parses a multipart request.
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        OutputStream os=response.getOutputStream();
        String localName=request.getPathInfo();
        File f=FileUtil.getFileInDirectory(baseDir, new File(localName));
        if(f.exists())
        {
            response.setContentType(MimeUtil.getMimeTypeFromExt(localName));
            StreamUtils.copyThenClose(new FileInputStream(f), response.getOutputStream());
        }
        else
        {
            response.setContentType(MimeUtil.TEXT_HTML);
            os.write("<HTML><H1>Error</H1><br/>Cannot find file: ".getBytes());
            os.write(localName.getBytes());
            os.write("</HTML>".getBytes());
        }
    }


    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "GETFile Servlet";
    }

}
