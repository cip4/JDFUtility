package org.cip4.JDFUtility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.cip4.jdflib.util.UrlUtil;

/**
 * 
 * @author rainer
 * 
 * 
 */
public class SendJDFServlet extends HttpServlet
{

	private final Log log;
	private static int nRequests = 0;

	/**
	 * 
	 */
	public SendJDFServlet()
	{
		super();
		log = LogFactory.getLog(getClass());
	}

	/**
     * 
     */
	private static final long serialVersionUID = -8902151736245089036L;

	/**
	 * Initializes the servlet.
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException
	{
		super.init(config);

	}

	/**
	 * Destroys the servlet.
	 */
	@Override
	public void destroy()
	{
		// foo
	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
	{
		log.warn("get not implemented");
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		log.debug("Processing request...");
		nRequests++;
		// Check that we have a file upload request
		final boolean isMultipart = FileUploadBase.isMultipartContent(request);
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
	private void processMultipartRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final List<FileItem> fileItems = JDFServletUtil.getFileList(request);

		FileItem fileItem = null;
		int nFiles = 0;
		String urlToSend = null;
		for (int i = 0; i < fileItems.size(); i++)
		{
			Runtime.getRuntime().gc(); // clean up before loading
			final FileItem item = fileItems.get(i);
			if (item.isFormField())
			{
				System.out.println("Form name: " + item.getFieldName());
				if (item.getFieldName().equals("sendURL"))
				{
					urlToSend = item.getString();
				}
			}
			else if (item.getSize() < 20 || item.getName().length() == 0)
			{
				log.warn("Bad File name: " + item.getName());
			}
			else
			// ok
			{
				log.info("File name: " + item.getName());
				fileItem = item;
				nFiles++;
			}
		}

		log.info("File count: " + nFiles);

		final XMLDoc htmlDoc = new XMLDoc("html", null);
		final KElement html = prepareHeader(urlToSend, htmlDoc);
		html.appendElement("h2").setText("Total Request Count: " + nRequests);
		html.appendElement("hr");
		if (fileItem != null)
		{
			processFileItem(fileItem, urlToSend, html);
		}

		// html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
		// Writes the XMP packet to output
		// Todo: Use JSP instead of writing directly to output

		writeOutput(response, htmlDoc);
	}

	/**
	 * @param response
	 * @param htmlDoc
	 * @throws IOException
	 */
	private void writeOutput(final HttpServletResponse response, final XMLDoc htmlDoc) throws IOException
	{
		response.setContentType("text/html;charset=utf-8");
		final OutputStream os = response.getOutputStream();
		htmlDoc.write2Stream(os, 2, false);
		os.flush();
		os.close();
	}

	/**
	 * @param fileItem
	 * @param urlToSend
	 * @param html
	 * @throws IOException
	 */
	private void processFileItem(final FileItem fileItem, final String urlToSend, final KElement html) throws IOException
	{
		// Get the first file item
		// To do: Process all file items
		html.appendElement("h3").setText("File size: " + fileItem.getSize() / 1024 + "KB");
		html.appendElement("h3").setText("File type: " + fileItem.getContentType());
		final InputStream ins = fileItem.getInputStream();
		final JDFParser p = new JDFParser();
		final JDFDoc d = p.parseStream(ins);
		if (d != null && urlToSend != null && (d.getJDFRoot() != null || d.getJMFRoot() != null))
		{
			final URL url = UrlUtil.stringToURL(urlToSend);

			final HttpURLConnection urlCon = d.write2HTTPURL(url, null);
			if (urlCon == null)
			{
				errorExit(html, "No connection established to " + urlToSend);
				return;
			}
			else if (urlCon.getResponseCode() != 200)
			{
				errorExit(html, "HTML Response code=" + urlCon.getResponseCode());
			}
			InputStream inStream;
			try
			{
				inStream = urlCon.getInputStream();
			}
			catch (final Exception x)
			{
				inStream = null;
			}

			JDFDoc docResp = null;
			final boolean success = urlCon.getResponseCode() == 200;
			String outFileName = null;
			if (inStream != null)
			{
				final JDFParser parser = new JDFParser();
				parser.parseStream(inStream);
				docResp = parser.getDocument() == null ? null : new JDFDoc(parser.getDocument());
				final File outFile = JDFServletUtil.getTmpFile("SendJDFTmp", fileItem, "jdf", ".jdf");
				if (docResp != null)
				{
					docResp.write2File(outFile.getAbsolutePath(), 2, true);
					outFileName = outFile.getName();
				}
			}

			log.info(success ? "Send was successful" : "Send Failed");

			// very basic html output
			html.appendElement("H2").appendText("Sent " + fileItem.getName() + " to " + urlToSend);
			html.appendElement("H3").appendText("Return Code: " + urlCon.getResponseCode());
			html.appendElement("H3").appendText("Headers:");
			final KElement list = html.appendElement("ul");
			final Map<String, List<String>> fields = urlCon.getHeaderFields();
			if (fields != null)
			{
				final Iterator<String> it = fields.keySet().iterator();
				while (it.hasNext())
				{
					final String key = it.next();
					final String value = urlCon.getHeaderField(key);
					list.appendElement("li").setText(key + ": " + value);
				}
			}

			if (success)
			{
				if (outFileName != null)
				{
					html.appendElement("H2").setText("Returned document");
					final KElement dl = html.appendElement("a");
					dl.appendText(outFileName);
					dl.setAttribute("href", "./SendJDFTmp/" + outFileName, null);
					html.appendElement("hr");
					if (docResp != null)
					{
						html.appendElement("pre").appendElement("code").setText(docResp.write2String(2));
					}
					html.appendElement("hr");
				}
				else
				{
					html.appendElement("H3").appendText("No Response Stream was received");
				}
			}
			else
			{
				final KElement e = html.getCreateXPathElement("H2/Font");
				e.setAttribute("color", "xff0000");
				e.appendText("Sending of " + fileItem.getName() + " to " + urlToSend + " failed!!! ");
			}
			JDFServletUtil.cleanup("SendJDFTmp");

			fileItem.delete();
		}
		else
		{
			html.appendText("file:" + fileItem.getName() + " not sent to  " + urlToSend + ". Submission failed!!! ");
		}
	}

	/**
	 * @param html
	 * @param string
	 */
	private void errorExit(final KElement html, final String string)
	{
		html.appendElement("h2").setText("Error");
		html.appendText(string);
	}

	/**
	 * @param urlToSend
	 * @param htmlDoc
	 * @return
	 */
	private KElement prepareHeader(final String urlToSend, final XMLDoc htmlDoc)
	{
		final KElement html = htmlDoc.getRoot();
		html.appendElement("LINK").setAttribute("HREF", "http://www.cip4.org/css/styles_pc.css");
		html.getElement("LINK").setAttribute("TYPE", "text/css");
		html.getElement("LINK").setAttribute("REL", "stylesheet");

		html.appendElement("head").appendElement("title").appendText("SendJDF " + urlToSend + " output");
		// html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
		html.appendElement("H1").appendText("SendJDF Response");
		html.appendElement("br");
		html.appendElement("H2").appendText("SendJDF " + urlToSend + " output");
		return html;
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "SendJDF Servlet";
	}

}
