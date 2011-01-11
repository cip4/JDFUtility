/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
public class SendJDFServlet extends UtilityServlet
{

	protected class SendCall extends ServletCall
	{
		/**
		 * @param utilityServlet
		 * @param request
		 * @param response
		 */
		public SendCall(UtilityServlet utilityServlet, HttpServletRequest request, HttpServletResponse response)
		{
			super(utilityServlet, request, response);
		}

		/**
		 * 
		 * @see org.cip4.jdfutility.ServletCall#processPost()
		 * @throws ServletException
		 * @throws IOException
		 */
		@Override
		protected void processPost() throws ServletException, IOException
		{
			final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart)
			{
				log.debug("Processing multipart request...");
				processMultipartRequest();
			}
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
		 * Parses a multipart request.
		 * @throws ServletException 
		 * @throws IOException 
		 */
		private void processMultipartRequest() throws ServletException, IOException
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
			html.appendElement("hr");
			if (fileItem != null)
			{
				processFileItem(fileItem, urlToSend, html);
			}

			// html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
			// Writes the XMP packet to output
			// Todo: Use JSP instead of writing directly to output

			writeOutput(htmlDoc);
		}

		/**
		 * @param htmlDoc
		 * @throws IOException
		 */
		private void writeOutput(final XMLDoc htmlDoc) throws IOException
		{
			response.setContentType("text/html;charset=utf-8");
			final OutputStream os = response.getOutputStream();
			htmlDoc.write2Stream(os, 2, false);
			os.flush();
			os.close();
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
	}

	/**
	 * 
	 */
	public SendJDFServlet()
	{
		super();
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
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "SendJDF Servlet";
	}

	/**
	 * @see org.cip4.jdfutility.UtilityServlet#getServletCall(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @param request
	 * @param response
	 * @return
	*/
	@Override
	protected ServletCall getServletCall(HttpServletRequest request, HttpServletResponse response)
	{
		return new SendCall(this, request, response);
	}

}
