
package org.cip4.JDFUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;

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
public class SendJDFServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(SendJDFServlet.class.getName());

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
        log.warn("get not implemented");
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        log.debug("Processing request...");

        // Check that we have a file upload request
        boolean isMultipart = FileUploadBase.isMultipartContent(request);
        if (isMultipart)
        {
            log.debug("Processing multipart request...");
            processMultipartRequest(request, response);
        }
        else
        {
            log.warn("Not a multipart request!");
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
        int nFiles=0;
        String urlToSend=null;
        for(int i=0;i<fileItems.size();i++)
        {
            Runtime.getRuntime().gc(); // clean up before loading
            FileItem item = (FileItem) fileItems.get(i);
            if(item.isFormField())
            {
                System.out.println("Form name: " + item.getFieldName());
                if(item.getFieldName().equals("sendURL"))
                {
                    urlToSend = item.getString();
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

        html.appendElement("head").appendElement("title").appendText("SendJDF "+urlToSend+" output");
//      html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
        html.appendElement("H1").appendText("SendJDF Response");
        html.appendElement("br");
        html.appendElement("H2").appendText("SendJDF "+urlToSend+" output");

        System.out.println("try");
        boolean success=false;

        if(fileItem!=null)
        {
            InputStream ins = fileItem.getInputStream();
            JDFParser p=new JDFParser();
            JDFDoc d=p.parseStream(ins);
            if(d!=null && urlToSend!=null && (d.getJDFRoot()!=null || d.getJMFRoot()!=null))
            {
                final URL url         = new URL(urlToSend);

                final HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
                urlCon.setDoOutput(true);
                urlCon.setRequestProperty("Connection", "close");
                urlCon.setRequestProperty("Content-Type", d.getContentType());

                d.write2Stream(urlCon.getOutputStream(), 0, true);
                InputStream inStream;
                try 
                {
                    inStream= urlCon.getInputStream();
                }
                catch (Exception x)
                {
                    inStream=null;
                }
                JDFDoc docResp=null;
                success=urlCon.getResponseCode()==200;             
                String outFileName=null;
                if(inStream!=null)
                {
                    final JDFParser parser = new JDFParser();
                    parser.parseStream(inStream); 
                    docResp= parser.getDocument()==null ? null : new JDFDoc(parser.getDocument());
                    File outFile=JDFServletUtil.getTmpFile("SendJDFTmp",fileItem,"jdf",".jdf");
                    if(docResp!=null)
                    {
                        docResp.write2File(outFile.getAbsolutePath(), 2, true);
                        outFileName=outFile.getName();
                    }
                }

                System.out.println(success ? "Send was successful" : "Send Failed");

                // very basic html output
                html.appendElement("H2").appendText("Sent "+fileItem.getName()+" to "+urlToSend);
                html.appendElement("H3").appendText("Return Code: "+urlCon.getResponseCode());
                html.appendElement("H3").appendText("Headers:");
                KElement list = html.appendElement("ul");
                Map fields=urlCon.getHeaderFields();
                if(fields!=null)
                {
                    Iterator it=fields.keySet().iterator();
                    while(it.hasNext())
                    {
                        String key=(String)it.next();
                        String value=urlCon.getHeaderField(key);
                        list.appendElement("li").setText(key+": "+value);
                    }
                }

                if(success)
                {
                    if(outFileName!=null)
                    {
                        html.appendElement("H2").setText("Returned document");
                        KElement dl=html.appendElement("a");
                        dl.appendText(outFileName);
                        dl.setAttribute("href","./SendJDFTmp/"+outFileName,null);
                        html.appendElement("hr");
                        html.appendElement("pre").appendElement("code").setText(docResp.write2String(2));
                        html.appendElement("hr");
                    }
                    else
                    {
                        html.appendElement("H3").appendText("No Response Stream was received");
                    }
                }
                else
                {
                    KElement e=html.getCreateXPathElement("H2/Font");
                    e.setAttribute("color","xff0000");
                    e.appendText("Sending of " +fileItem.getName()+ " to "+urlToSend+" failed!!! ");                
                }
                JDFServletUtil.cleanup("SendJDFTmp");

                fileItem.delete();
            }
            else
            {
                html.appendText("file:"+fileItem.getName() +" not sent to  "+urlToSend+". Submission failed!!! ");                                
            }
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
