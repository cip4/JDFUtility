/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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
import org.cip4.jdflib.core.JDFElement.EnumValidationLevel;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.validate.JDFValidator;

/**
 * This servlet parses any file and returns any XMP packet found in the file.
 *
 * @author claes
 *
  */
public class CheckJDFServlet extends UtilityServlet
{
	public CheckJDFServlet()
	{
		super();
	}

	/**
	 *
	 * @author rainer prosi
	 *
	 */
	protected class CheckJDFCall extends ServletCall
	{

		/**
		 * @param utilityServlet
		 * @param request
		 * @param response
		 */
		public CheckJDFCall(final UtilityServlet utilityServlet, final HttpServletRequest request, final HttpServletResponse response)
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
	private static final long serialVersionUID = -3663640051616511411L;

	/**
	 * Parses a multipart request.
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processMultipartRequest(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		// Parse the multipart request
		final List<FileItem> fileItems = new FileItemList(request, 42 * 1024 * 1024).getFileList(true, true);

		// Get the first file item
		// To do: Process all file items
		FileItem fileItem = null;
		boolean bUseSchema = false;
		boolean bIgnorePrivate = false;
		boolean prettyFormat = false;

		String language = "EN";
		String devcapName = null;
		File devcapFile = null;
		for (final FileItem item : fileItems)
		{
			final String fieldName = item.getFieldName();

			if (item.isFormField())
			{
				if ("UseSchema".equals(fieldName) && "true".equals(item.getString()))
				{
					bUseSchema = true;
				}
				else if ("IgnorePrivate".equals(fieldName) && "true".equals(item.getString()))
				{
					bIgnorePrivate = true;
				}
				else if ("PrettyFormat".equals(fieldName) && "true".equals(item.getString()))
				{
					prettyFormat = true;
				}
				else if (fieldName.equals("Language"))
				{
					language = item.getString();
					if (language.equalsIgnoreCase("nederlands"))
					{
						language = "nl";
					}
					if (language.equalsIgnoreCase("deutsch"))
					{
						language = "de";
					}
					language = language.substring(0, 2).toUpperCase();
					log.debug("Language: " + language);
				}
			}
			else if ("devcapFile".equals(fieldName))
			{
				devcapName = item.getName();
				log.info("devcapFile: " + devcapName);
				if (devcapFile != null && devcapFile.length() > 0)
				{
					devcapFile = createTmpFile(item, "CheckJDFTmp", "devcap");
				}
				else
				{
					devcapFile = null;
				}
			}
			else if (item.getSize() < 20 || item.getName().length() == 0)
			{
				log.warn("Bad File name: " + item.getName());
			}
			else
			{
				log.info("File name: " + item.getName());
				fileItem = item;
			}
		}
		String xmlString = null;
		XMLDoc d = null;

		// Extracts JDF packet
		try
		{

			final JDFValidator checker = new JDFValidator();

			checker.setPrint(false);
			checker.bQuiet = true;
			checker.setIgnorePrivate(bIgnorePrivate);
			checker.level = EnumValidationLevel.Complete;
			if (prettyFormat)
				checker.xslStyleSheet = "./checkjdf.xsl";

			if (devcapFile != null && devcapFile.canRead())
			{
				checker.devCapFile = devcapFile.getAbsolutePath();
				log.info("Devcap file: " + devcapFile.getAbsolutePath());
			}

			if (bUseSchema) // using schema
			{
				String schemaPath = JDFServletUtil.baseDir + "Schema/JDF.xsd";
				final File fs = new File(schemaPath);
				schemaPath = UrlUtil.getRelativeURL(fs, null, true);

				checker.setJDFSchemaLocation("https://toolbox.cip4.org/jdf/Schema/JDF.xsd");
			}

			if (fileItem != null)
			{
				final String fileItemName = fileItem.getName();
				log.info("fileItem Name: " + fileItemName);
				final String lower = fileItemName.toLowerCase();
				if (lower.endsWith(".zip"))
				{
					final File zipFile = createTmpFile(fileItem, "CheckJDFTmp", "zip");
					d = checker.processZipFile(zipFile);
				}
				else if (lower.endsWith(".mjm") || lower.endsWith(".mjd") || lower.endsWith(".mim"))
				{
					log.info("processing MIME file");
					final InputStream s = fileItem.getInputStream();
					d = checker.processMimeStream(s);
				}
				else
				{
					final InputStream s = fileItem.getInputStream();
					d = checker.processSingleStream(s, fileItemName, null);
					d.setXSLTURL(checker.xslStyleSheet);
				}
				final KElement root = d.getRoot();
				root.setAttribute("Language", language);

				if (prettyFormat)
					d.setXSLTURL("./checkjdf.xsl");

				xmlString = d.write2String(2);
				fileItem.delete();
				JDFServletUtil.cleanup("CheckJDFTmp");
			}
			else
			{
				d = checker.processSingleStream(null, null, null);
				xmlString = d.write2String(2);
			}
		}
		catch (final IOException ioe)
		{
			throw new ServletException("Could not read file.", ioe);
		}
		response.setContentType("text/xml;charset=utf-8");

		final PrintWriter out = response.getWriter();
		out.println(xmlString);
		out.flush();
		out.close();
	}

	// //////////////////////////////////////////////////////////////////

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "JDFValidator Servlet";
	}

	/**
	 * @see org.cip4.jdfutility.UtilityServlet#getServletCall(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @param request
	 * @param response
	 * @return
	*/
	@Override
	protected ServletCall getServletCall(final HttpServletRequest request, final HttpServletResponse response)
	{
		return new CheckJDFCall(this, request, response);
	}
}
