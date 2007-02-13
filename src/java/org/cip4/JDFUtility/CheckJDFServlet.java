/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2006 The International Cooperation for the Integration of 
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
import java.io.FileNotFoundException;
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
import org.apache.commons.io.IOUtils;
import org.cip4.jdflib.CheckJDF;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.KElement.EnumValidationLevel;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * This servlet parses any file and returns any XMP packet found in the file.
 *
 * @author  claes
 *
 * @web:servlet	name="XMPServlet" 
 *						display-name="XMP Servlet" 
 *						description="" 
 *						load-on-startup="1"
 *
 * @web:servlet-init-param	name="" 
 *									value=""
 *									description=""
 *
 * @web:servlet-mapping url-pattern="/xmpservlet"
 */
public class CheckJDFServlet extends HttpServlet {
    
    /**
     * 
     */
    private static final long serialVersionUID = -3663640051616511411L;
    
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
    }
    
    /** Destroys the servlet.
     */
    public void destroy() {
        // foo
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        // foo
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        System.out.println();
        System.out.println("Processing request...");
        
        // Check that we have a file upload request
        
        boolean isMultipart = FileUploadBase.isMultipartContent(request);
        if (isMultipart)
        {
            System.out.println("Processing multipart request...");
            processMultipartRequest(request, response);
        }
    }
    
    
    /**
     * Parses a multipart request.
     */
    private void processMultipartRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        // Parse the multipart request
        List fileItems = JDFServletUtil.getFileList(request);
        
        // Get the first file item
        // To do: Process all file items
        FileItem fileItem=null;
        boolean bUseSchema=false;
        boolean bIgnorePrivate=false;
        String language="EN";
        for(int i=0;i<fileItems.size();i++)
        {
            Runtime.getRuntime().gc(); // clean up before loading
            FileItem item = (FileItem) fileItems.get(i);
            if(item.isFormField())
            {
                final String fieldName = item.getFieldName();
                System.out.println("Form name: " + fieldName);
                if(fieldName.equals("UseSchema"))
                {
                    if(item.getString().equals("true"))
                        bUseSchema=true;
                }        
                else if(fieldName.equals("IgnorePrivate"))
                {
                    if(item.getString().equals("true"))
                        bIgnorePrivate=true;
                    System.out.println("IgnorePrivate: " + bIgnorePrivate);
                }        
                else if(fieldName.equals("Language"))
                {
                    language=item.getString();
                    language=language.substring(0,2).toUpperCase();
                    System.out.println("Language: " + language);
                }        
            }
            else if (item.getSize()<20 || item.getName().length()==0)
            {
                System.out.println("Bad File name: " + item.getName());
            }
            else // ok
            {
                System.out.println("File name: " + item.getName());
                fileItem=item;
            }
        }
        String xmlString=null;
        XMLDoc d=null;
        
        // Extracts JDF packet
        try
        {
            
            CheckJDF checker=new CheckJDF();
            
            checker.setPrint(false);
            checker.bQuiet=true;
            checker.setIgnorePrivate(bIgnorePrivate);
            checker.level=EnumValidationLevel.Complete;
            
            if(bUseSchema) // using schema
            {
                String schemaPath=JDFServletUtil.baseDir+"Schema/JDF.xsd";
                File fs=new File(schemaPath);
                schemaPath=UrlUtil.getRelativeURL(fs, null, true);
                
                checker.setJDFSchemaLocation(new File(schemaPath));
            }
            
            if(fileItem!=null)
            {
                final String fileItemName = fileItem.getName();
                System.out.println("FIName: "+fileItemName);
                if(fileItemName.toLowerCase().endsWith(".zip"))
                {
                    File zipFile = createTmpZipFile(fileItem);
                    d=checker.processZipFile(zipFile);
                }
                else if(fileItemName.toLowerCase().endsWith(".mjm"))
                {
                    
                    System.out.println("processing MIME file");
                    InputStream s = fileItem.getInputStream();
                    d=checker.processMimeStream(s);
                }
                else
                {
                    InputStream s = fileItem.getInputStream();
                    d=checker.processSingleStream(s,fileItemName,null);
                }
                d.getRoot().setAttribute("Language",language);
                File outFile=JDFServletUtil.getTmpFile("CheckJDFTmp",fileItem,"check_",".xml");
                KElement root=d.getRoot();
                String sURL="./CheckJDFTmp/"+outFile.getName();
                sURL= StringUtil.escape(sURL,UrlUtil.m_URIEscape,"%",16,2,0x21,-1);           
                root.setAttribute("XMLUrl",sURL);
                // MS IE sucks! replace non escape with __
                root.setAttribute("XMLFile",fileItemName);
                System.out.println("URL: "+sURL);
                //root.setAttribute("XMLUrl","./CheckJDFTmp/"+outFile.getName());
                d.write2File(outFile.getPath(), 2, true);
                d.setXSLTURL("./checkjdf.xsl");
                xmlString=d.write2String(0);
                fileItem.delete();
                JDFServletUtil.cleanup("CheckJDFTmp");
            }
            else
            {
                d=checker.processSingleStream(null,null,null);
                d.setXSLTURL("./checkjdf.xsl");
                xmlString=d.write2String(0);                
            }
        }
        catch(IOException ioe)
        {
            throw new ServletException("Could not read file.", ioe);
        }
        response.setContentType("text/xml;charset=utf-8");
       
        
        PrintWriter out = response.getWriter();
        out.println(xmlString);
        out.flush();
        out.close();
        
        System.out.println("Exit processMultipartRequest");
    }

    ////////////////////////////////////////////////////////////////////
    
    private File createTmpZipFile(FileItem fileItem) throws FileNotFoundException, IOException
    {
        InputStream s = fileItem.getInputStream();
        File zipFile=JDFServletUtil.getTmpFile("CheckJDFTmp",fileItem,"zip_",".zip");
        FileOutputStream fos=new FileOutputStream(zipFile);
        int n=IOUtils.copy(s,fos);
        fos.flush();
        fos.close();
        System.out.println("ZIP file name: "+zipFile.toString()+" size: "+n);
        return zipFile;
    }
    
    ////////////////////////////////////////////////////////////////////
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "CheckJDF Servlet";
    }
    
}
