/*
 * XMPServlet.java
 *
 * Created on den 30 oktober 2003, 19:02
 */

package org.cip4.JDFUtility;

import java.io.File;
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
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cip4.jdflib.CheckJDF;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.core.KElement.EnumValidationLevel;
import org.cip4.jdflib.util.StringUtil;

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
        
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
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
        int iLen= 0;
        for(int i=0;i<fileItems.size();i++)
        {
            Runtime.getRuntime().gc(); // clean up before loading
            FileItem item = (FileItem) fileItems.get(i);
            if(item.isFormField())
            {
                System.out.println("Form name: " + item.getFieldName());
                if(item.getFieldName().equals("UseSchema"))
                {
                    if(item.getString().equals("true"))
                        iLen=2;
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
        
        // Extracts JDF packet
        try
        {
            InputStream s = fileItem==null ? null : fileItem.getInputStream();
            
            CheckJDF checker=new CheckJDF();
            
            checker.setPrint(false);
            checker.bQuiet=true;
            checker.setIgnorePrivate(false);
            checker.level=EnumValidationLevel.Complete;

            if(iLen>0) // using schema
            {
                String schemaPath=JDFServletUtil.baseDir+"Schema/JDF.xsd";
                File fs=new File(schemaPath);
                schemaPath=StringUtil.getRelativeURL(fs,null);
                
                checker.setJDFSchemaLocation(schemaPath);
            }
            
            if(fileItem!=null)
            {
                final String fileItemName = fileItem.getName();
                System.out.println("FIName: "+fileItemName);
                XMLDoc d=checker.processSingleFile(s,null,fileItemName);
                File outFile=JDFServletUtil.getTmpFile("CheckJDFTmp",fileItem,"check_",".xml");
                KElement root=d.getRoot();
                String sURL="./CheckJDFTmp/"+outFile.getName();
                sURL= StringUtil.escape(sURL,StringUtil.m_URIEscape,"%",16,2,0x21,-1);           
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
                XMLDoc d=checker.processSingleFile(s,null,null);
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
    }
    
    
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "CheckJDF Servlet";
    }
    
}
