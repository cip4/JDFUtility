
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
import org.apache.commons.fileupload.FileUploadBase;
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
public class FixJDFServlet extends HttpServlet {
    
    
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
        List fileItems = JDFServletUtil.getFileList(request);
        
        FileItem fileItem=null;
        EnumVersion version=null;
        int nFiles=0;
        String versionField="";
        for(int i=0;i<fileItems.size();i++)
        {
            Runtime.getRuntime().gc(); // clean up before loading
            FileItem item = (FileItem) fileItems.get(i);
            if(item.isFormField())
            {
                System.out.println("Form name: " + item.getFieldName());
                if(item.getFieldName().equals("Version"))
                {
                    versionField = item.getString();
                    if(versionField.startsWith("1."))
                    {
                      version=EnumVersion.getEnum(versionField);
                      versionField=version.getName();                        
                    }
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
                nFiles++;
            }
        }
        
        System.out.println("File count: " + nFiles);
        if(fileItem!=null)
        {
            // Get the first file item
            // To do: Process all file items
            System.out.println("File size: " + fileItem.getSize()/1024 + "KB");
            System.out.println("File type: " + fileItem.getContentType());
        }
         
        XMLDoc htmlDoc=new XMLDoc("html",null);
        KElement html=htmlDoc.getRoot();
        html.appendElement("LINK").setAttribute("HREF","http://www.cip4.org/css/styles_pc.css");
        html.getElement("LINK").setAttribute("TYPE","text/css");
        html.getElement("LINK").setAttribute("REL","stylesheet");
        
        html.appendElement("head").appendElement("title").appendText("FixJDF "+versionField+" output");
//        html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
        html.appendElement("H1").appendText("FixJDF "+versionField+" output");
       
        // Extracts XMP packet
        try
        {
            System.out.println("try");
            boolean success=false;
            
            if(fileItem!=null)
            {
            InputStream ins = fileItem.getInputStream();
            JDFParser p=new JDFParser();
            JDFDoc d=p.parseStream(ins);
            if(d!=null){
                KElement k=d.getRoot();
                if (k instanceof JDFElement)
                {
                    System.out.print("Updating to "+versionField+" ... ");                    

                    if(versionField.equals("General"))
                    {
                        version=null;
                    }
                    
                    JDFNode theRoot=d.getJDFRoot();
                    if(theRoot!=null) // it is a JDF
                    {
                        if(versionField.equals("Retain")&&theRoot.hasAttribute(AttributeName.VERSION))
                        {
                            version=theRoot.getVersion(true);
                            versionField=version.getName();
                        }
                        JDFAuditPool ap=theRoot.getCreateAuditPool();
                        JDFModified modi=ap.addModified("FixJDF Web Service Build: "+JDFAudit.software(),null);
                        modi.setDescriptiveName("update to version "+versionField);
                    }
                    else // might be a JMF
                    {
                        JDFJMF theJMF=d.getJMFRoot();
                        if(theJMF!=null && versionField.equals("Retain")&& theJMF.hasAttribute(AttributeName.VERSION))
                        {
                            version=theJMF.getVersion(true);
                            versionField=version.getName();
                        }
                    }
                    JDFElement e = (JDFElement) k;
                    success=e.fixVersion(version);                    
                    System.out.println(success ? "Fix was successful" : "Fix Failed");
                }
            }
            
            File outFile=JDFServletUtil.getTmpFile("FixJDFTmp",fileItem,"jdf"+versionField,".jdf");
            String outFileName=outFile.getName();
            if(d!=null)
            {
                d.write2File(outFile.getAbsolutePath(), 2, true);
            }
            
            // very basic html output
            if(success)
            {
                html.appendText("DownLoad updated "+versionField+" version of "+fileItem.getName()+" here: ");
                KElement dl=html.appendElement("a");
                dl.appendText(outFileName);
                dl.setAttribute("href","./FixJDFTmp/"+outFileName,null);
            }
            else
            {
                KElement e=html.getCreateXPathElement("H2/Font");
                e.setAttribute("color","xff0000");
                e.appendText("Update of " +fileItem.getName()+ " to JDF "+versionField+" failed!!! ");                
            }
            JDFServletUtil.cleanup("FixJDFTmp");
            
            fileItem.delete();
            }
            else
            {
                html.appendText("No file to update to JDF "+versionField+". Update failed!!! ");                                
            }
        }
        catch(IOException ioe)
        {
            throw new ServletException("Could not read file.", ioe);
        }
        
 //       html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
        // Writes the XMP packet to output
        // Todo: Use JSP instead of writing directly to output
        
        response.setContentType("text/html;charset=utf-8");
       
        PrintWriter out = response.getWriter();
        out.println(htmlDoc.write2String(0));
        
    }
    
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "FixJDF Servlet";
    }
    
}
