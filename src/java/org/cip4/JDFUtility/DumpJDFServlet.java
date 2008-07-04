
package org.cip4.JDFUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.mail.Multipart;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.lf5.util.StreamUtils;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;

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

    private DumpDir baseDir=null;
    private HashMap<File, DumpDir> subDumps=new HashMap<File, DumpDir>();
    /**
     * 
     */
    private static final long serialVersionUID = -8902151736245089036L;


    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final String root = config.getInitParameter("rootDir");
        System.out.println("Config root: "+root);
        File rootFile = new File(root);
        baseDir=new DumpDir(rootFile);
        subDumps.put(rootFile, baseDir);
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
        //System.out.println("dump service");

        String dir=request.getPathInfo();
        File newDir=dir==null ? baseDir.getDir() : FileUtil.getFileInDirectory(baseDir.getDir(), new File(dir));
        if(newDir.exists() && ! newDir.isDirectory())
            newDir=baseDir.getDir();
        else
            newDir.mkdirs();
        DumpDir theDump=getCreateDump(newDir);
        File f=theDump.newFile();

        try
        {

            FileOutputStream fs=new FileOutputStream(f);
            PrintWriter w=new PrintWriter(fs);
//          w.println("Context Type:"+http);
            w.println("Context Path:"+request.getContextPath());

            final String contentType = request.getContentType();
            w.println("Context Type:"+contentType);
            w.println("Context Length:"+request.getContentLength());
            w.print("------ end of http header ------\n");
            w.flush();

            StreamUtils.copyThenClose(request.getInputStream(), fs);
            OutputStream os=response.getOutputStream();
            w=new PrintWriter(os);
            w.print("<HTML><HEAD><TITLE>JDF Test DUMP</TITLE></HEAD>");
            w.print("<H1>Request Dump</H1><Body>");
            w.println("Context Path:"+newDir+"<BR/>");
            if(contentType!=null)
                w.println("Content Type:"+contentType+"<BR/>");
            w.print("</Body></HTML>");
            w.flush();

            if(contentType!=null && contentType.toLowerCase().startsWith("multipart/related"))
            {
                FileInputStream fis=new FileInputStream(f);
                final String dirName = StringUtil.newExtension(f.getPath(),".dir");
                System.out.println("dump mime: "+dirName);
                char c='a';
                while(c!='!')
                    c=(char)fis.read();
                fis.read();
                fis.read();

                Multipart mp=MimeUtil.getMultiPart(fis);
                MimeUtil.writeToDir(mp, new File(dirName));
            }
        }
        catch (Exception e) {
            System.out.println("dump service - snafu: "+e);
        }

    }

    /**
     * @param newDir
     */
    private DumpDir getCreateDump(File newDir)
    {
        synchronized (subDumps)
        {

            DumpDir theDump=subDumps.get(newDir);
            if(theDump==null)
            {
                theDump=new DumpDir(newDir);
                subDumps.put(newDir, theDump);
            }
            return theDump;
        }
    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "DumpJDF Servlet";
    }

}
