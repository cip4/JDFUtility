/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of 
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of 
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration 
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software 
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG 
 * copyright (c) 1999-2001, Agfa-Gevaert N.V. 
 *  
 * For more information on The International Cooperation for the 
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *  
 * 
 */
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
        baseDir.quiet=false;
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
        String header="Context Path: "+request.getRequestURI();
        String contentType = request.getContentType();
        header+="\nContext Type: "+contentType;
        header+="\nContext Length: "+request.getContentLength();

 
        try
        {

            File f=theDump.newFileFromStream(header, request.getInputStream());
            OutputStream os=response.getOutputStream();
            PrintWriter w=new PrintWriter(os);
            w.print("<HTML><HEAD><TITLE>JDF Test DUMP</TITLE></HEAD>");
            w.print("<H1>Request Dump</H1><Body>");
            w.println("Context Path:"+newDir+"<BR/>");
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
                theDump.quiet=false;

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
