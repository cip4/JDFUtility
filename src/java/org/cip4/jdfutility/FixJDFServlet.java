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
package org.cip4.jdfutility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.pool.JDFAuditPool;
import org.cip4.jdflib.resource.JDFModified;

/**
 *
 * @author  rainer
 *
 *
 */
public class FixJDFServlet extends UtilityServlet
{
	protected class FixCall extends ServletCall
	{
		/**
		 * @param utilityServlet
		 * @param request
		 * @param response
		 */
		public FixCall(UtilityServlet utilityServlet, HttpServletRequest request, HttpServletResponse response)
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
				processMultipartRequest(request, response);
			}
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8902151736245089036L;

	/**
	 * Parses a multipart request.
	 * @param request 
	 * @param response 
	 * @throws ServletException 
	 * @throws IOException 
	 */
	protected void processMultipartRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		List<FileItem> fileItems = JDFServletUtil.getFileList(request);

		FileItem fileItem = null;
		EnumVersion version = null;
		int nFiles = 0;
		String versionField = "";
		for (int i = 0; i < fileItems.size(); i++)
		{
			Runtime.getRuntime().gc(); // clean up before loading
			FileItem item = fileItems.get(i);
			if (item.isFormField())
			{
				log.info("Form name: " + item.getFieldName());
				if (item.getFieldName().equals("Version"))
				{
					versionField = item.getString();
					if (versionField.startsWith("1."))
					{
						version = EnumVersion.getEnum(versionField);
						versionField = version.getName();
					}
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
		if (fileItem != null)
		{
			// Get the first file item
			// To do: Process all file items
			log.info("File size: " + fileItem.getSize() / 1024 + "KB");
			log.info("File type: " + fileItem.getContentType());
		}

		XMLDoc htmlDoc = new XMLDoc("html", null);
		KElement html = htmlDoc.getRoot();
		html.appendElement("LINK").setAttribute("HREF", "http://www.cip4.org/css/styles_pc.css");
		html.getElement("LINK").setAttribute("TYPE", "text/css");
		html.getElement("LINK").setAttribute("REL", "stylesheet");

		html.appendElement("head").appendElement("title").appendText("FixJDF " + versionField + " output");
		//        html.appendXMLComment("#include virtual=\"/global/navigation/menue_switch.php?section=support\" ");
		html.appendElement("H1").appendText("FixJDF " + versionField + " output");

		// Extracts XMP packet
		try
		{
			log.debug("try");
			boolean success = false;

			if (fileItem != null)
			{
				InputStream ins = fileItem.getInputStream();
				JDFParser p = new JDFParser();
				JDFDoc d = p.parseStream(ins);
				if (d != null)
				{
					KElement k = d.getRoot();
					if (k instanceof JDFElement)
					{
						log.info("Updating to " + versionField + " ... ");

						if (versionField.equals("General"))
						{
							version = null;
						}

						JDFNode theRoot = d.getJDFRoot();
						if (theRoot != null) // it is a JDF
						{
							if (versionField.equals("Retain") && theRoot.hasAttribute(AttributeName.VERSION))
							{
								version = theRoot.getVersion(true);
								versionField = version.getName();
							}
							JDFAuditPool ap = theRoot.getCreateAuditPool();
							JDFModified modi = ap.addModified("FixJDF Web Service Build: " + JDFAudit.software(), null);
							modi.setDescriptiveName("update to version " + versionField);
						}
						else
						// might be a JMF
						{
							JDFJMF theJMF = d.getJMFRoot();
							if (theJMF != null && versionField.equals("Retain") && theJMF.hasAttribute(AttributeName.VERSION))
							{
								version = theJMF.getVersion(true);
								versionField = version.getName();
							}
						}
						JDFElement e = (JDFElement) k;
						success = e.fixVersion(version);
						log.info(success ? "Fix was successful" : "Fix Failed");
					}
				}

				File outFile = JDFServletUtil.getTmpFile("FixJDFTmp", fileItem, "jdf" + versionField, ".jdf");
				String outFileName = outFile.getName();
				if (d != null)
				{
					d.write2File(outFile.getAbsolutePath(), 2, true);
				}

				// very basic html output
				if (success)
				{
					html.appendText("DownLoad updated " + versionField + " version of " + fileItem.getName() + " here: ");
					KElement dl = html.appendElement("a");
					dl.appendText(outFileName);
					dl.setAttribute("href", "./FixJDFTmp/" + outFileName, null);
				}
				else
				{
					KElement e = html.getCreateXPathElement("H2/Font");
					e.setAttribute("color", "xff0000");
					e.appendText("Update of " + fileItem.getName() + " to JDF " + versionField + " failed!!! ");
				}
				JDFServletUtil.cleanup("FixJDFTmp");

				fileItem.delete();
			}
			else
			{
				html.appendText("No file to update to JDF " + versionField + ". Update failed!!! ");
			}
		}
		catch (IOException ioe)
		{
			log.error("Could not read file", ioe);
			throw new ServletException("Could not read file.", ioe);
		}

		response.setContentType("text/html;charset=utf-8");

		PrintWriter out = response.getWriter();
		out.println(htmlDoc.write2String(0));

	}

	/** Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "FixJDF Servlet";
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
		return new FixCall(this, request, response);
	}

}
