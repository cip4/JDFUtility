/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2021 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.jdfutility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import org.apache.commons.io.IOUtils;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFAudit;
import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumValidationLevel;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.extensions.xjdfwalker.XJDFToJDFConverter;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.pool.JDFAuditPool;
import org.cip4.jdflib.resource.JDFModified;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;
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

	private static final String CHECK_JDF_TMP = "CheckJDFTmp";
	private static final String FIX_JDF_TMP = "FixJDFTmp";
	private static final String FIX_JDF = "FixJDF";

	public CheckJDFServlet()
	{
		super();
		extractResources();
	}

	void extractResources()
	{
		extractResource("/index.html");
		extractResource("/index.js");
		extractResource("/index.css");
		extractResource("/checkjdf.xsl");
	}

	void extractResource(final String res)
	{
		final Class<? extends CheckJDFServlet> myClass = getClass();
		final InputStream listStream = myClass.getResourceAsStream(res);
		final File out = new File(JDFServletUtil.cwd, res);
		if (!out.exists())
		{
			FileUtil.streamToFile(listStream, out);
			log.info("copying resource to " + out.getAbsolutePath());
		}
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
			final boolean mp = ServletFileUpload.isMultipartContent(request);
			if (mp)
			{
				processMultipartRequest(request, response);
			}
		}
	}

	class FixCall extends ServletCall
	{
		/**
		 * @param utilityServlet
		 * @param request
		 * @param response
		 */
		public FixCall(final UtilityServlet utilityServlet, final HttpServletRequest request, final HttpServletResponse response)
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
			if (ServletFileUpload.isMultipartContent(request))
			{
				doFix(request, response);
			}
		}

		/**
		 * Parses a multipart request.
		 *
		 * @param request
		 * @param response
		 * @throws ServletException
		 * @throws IOException
		 *
		 */
		protected void doFix(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
		{
			final List<FileItem> fileItems = new FileItemList(request, 20l * 1024 * 1024).getFileList(true, true);
			FileItem fileItem = null;
			EnumVersion version = null;
			int nFiles = 0;
			String versionField = "";
			for (final FileItem item : fileItems)
			{
				if (item.isFormField())
				{
					log.info("Form name: " + item.getFieldName());
					if (item.getFieldName().equals("Version"))
					{
						versionField = item.getString();
						version = EnumVersion.getEnum(versionField);
						versionField = version.getName();
					}
				}
				else if (item.getSize() < 20 || StringUtil.isEmpty(item.getName()))
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

			final XMLDoc htmlDoc = XMLDoc.parseFile("index.html");
			final KElement html = htmlDoc.getRoot();

			html.setXPathValue("head/title", "FixJDF " + versionField + " output");
			final KElement head = html.getElement("head");
			head.removeChildren("script", null);

			final KElement body0 = html.getElement("body");
			body0.removeChildren("div", null);
			body0.removeChildren("nav", null);
			KElement div = body0.appendElement("div");
			div.setAttribute("class", "row");
			div.setAttribute("id", "fix-jdf");
			div = div.appendElement("div");
			div.setAttribute("class", "col-12");
			div.appendElement("h1").appendText("FixJDF " + versionField + " output");

			try
			{
				if (fileItem != null)
				{
					final InputStream ins = fileItem.getInputStream();
					final JDFDoc d0 = JDFDoc.parseStream(ins);
					final JDFDoc d = updateSingle(version, d0);

					final String extension = getExtension(d);
					final File outFile = JDFServletUtil.getTmpFile(FIX_JDF_TMP, fileItem, extension + "_" + versionField, "." + extension);
					final String outFileName = outFile.getName();
					if (d != null)
					{
						d.write2File(outFile.getAbsolutePath(), 2, true);
						div.appendText("DownLoad updated " + versionField + " version of " + fileItem.getName() + " here: ");
						final KElement dl = div.appendElement("a");
						dl.appendText(outFileName);
						dl.setAttribute("href", "FixJDFServlet/" + FIX_JDF_TMP + "/" + outFileName, null);
					}
					else
					{
						final KElement e = div.getCreateXPathElement("h2/Font");
						e.setAttribute("color", "xff0000");
						e.appendText("Update of " + fileItem.getName() + " to JDF " + versionField + " failed!!! ");
					}
					JDFServletUtil.cleanup(FIX_JDF_TMP);

					fileItem.delete();
				}
				else
				{
					html.appendText("No file to update to JDF " + versionField + ". Update failed!!! ");
				}
			}
			catch (final IOException ioe)
			{
				log.error("Could not read file", ioe);
				throw new ServletException("Could not read file.", ioe);
			}

			response.setContentType("text/html;charset=utf-8");

			final PrintWriter out = response.getWriter();
			out.println(htmlDoc.write2String(0));

		}

		/**
		 * @see org.cip4.jdfutility.ServletCall#processGet()
		 */
		@Override
		protected void processGet() throws IOException, ServletException
		{
			final String servletPath = request.getRequestURI();
			if (servletPath.contains(FIX_JDF_TMP))
			{
				final File dir = new File(JDFServletUtil.baseDir, FIX_JDF_TMP);
				final File f = new File(dir, StringUtil.token(servletPath, -1, "/"));
				if (f.exists())
				{
					final BufferedOutputStream o = new BufferedOutputStream(response.getOutputStream());
					final BufferedInputStream i = FileUtil.getBufferedInputStream(f);
					response.setContentType(UrlUtil.getMimeTypeFromURL(f.getName()));
					IOUtils.copyLarge(i, o);
					i.close();
					o.flush();
					o.close();
					doc = null;
				}
				else
				{
					response.sendError(404, "no such file: " + f.getAbsolutePath());
				}
			}
			else
			{
				super.processGet();
			}
		}

		private String getExtension(final JDFDoc d)
		{
			final KElement e = d == null ? null : d.getRoot();
			if (e == null)
				return null;
			return e.getLocalName().toLowerCase();
		}

		JDFDoc updateSingle(EnumVersion version, final JDFDoc d0)
		{
			final KElement k = d0 == null ? null : d0.getRoot();

			final JDFDoc d;
			if (!XJDFHelper.isXJDF(k) && !XJDFHelper.isXJMF(k) && version != null && version.isXJDF())
			{
				final XJDF20 jdfToXJDFConverter = new XJDF20();
				final KElement converted = jdfToXJDFConverter.convert(k);
				d = converted == null ? null : new JDFDoc(converted.getOwnerDocument());
			}
			else
			{
				if ((XJDFHelper.isXJDF(k) || XJDFHelper.isXJMF(k)) && version != null && !version.isXJDF())
				{
					final XJDFToJDFConverter xjdfToJDFConverter = new XJDFToJDFConverter(null);
					d = xjdfToJDFConverter.convert(k);
				}
				else
				{
					d = d0;
				}
				if (d != null)
				{
					if (k instanceof JDFElement)
					{
						log.info("Updating to " + version + " ... ");

						final JDFNode theRoot = d.getJDFRoot();
						if (theRoot != null) // it is a JDF
						{
							if (version == null && theRoot.hasAttribute(AttributeName.VERSION))
							{
								version = theRoot.getVersion(true);
							}
							final JDFAuditPool ap = theRoot.getCreateAuditPool();
							final JDFModified modi = ap.addModified("FixJDF Web Service Build: " + JDFAudit.software(), null);
							modi.setDescriptiveName("update to version " + version);
						}
						else
						// might be a JMF
						{
							final JDFJMF theJMF = d.getJMFRoot();
							if (theJMF != null && version == null && theJMF.hasAttribute(AttributeName.VERSION))
							{
								version = theJMF.getVersion(true);
							}
						}
						final JDFElement e = (JDFElement) k;
						e.fixVersion(version);
					}
				}
			}
			return d;
		}

	}

	/**
	 *
	 */
	private static final long serialVersionUID = -366364005161651141L;

	/**
	 * Parses a multipart request.
	 *
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void processMultipartRequest(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
	{
		// Parse the multipart request
		final List<FileItem> fileItems = new FileItemList(req, 43l * 1024l * 1024l).getFileList(true, true);

		// Get the first file item
		// To do: Process all file items
		FileItem fileItem = null;
		boolean bUseSchema = false;
		boolean bIgnorePrivate = false;
		boolean prettyFormat = false;

		String language = "EN";
		final String validationLevel = EnumValidationLevel.Complete.getName();
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
				else if ("Language".equals(fieldName))
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
				else if ("ValidationLevel".equals(fieldName))
				{
					language = item.getString();
				}
			}
			else if ("devcapFile".equals(fieldName))
			{
				devcapName = item.getName();
				log.info("devcapFile: " + devcapName);
				if (devcapFile != null && devcapFile.length() > 0)
				{
					devcapFile = createTmpFile(item, CHECK_JDF_TMP, "devcap");
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

			final JDFValidator checker = getChecker(bUseSchema, bIgnorePrivate, prettyFormat, validationLevel, devcapFile);

			if (fileItem != null)
			{
				final String fileItemName = fileItem.getName();
				log.info("fileItem Name: " + fileItemName);
				final String lower = fileItemName.toLowerCase();
				if (lower.endsWith(".zip"))
				{
					final File zipFile = createTmpFile(fileItem, CHECK_JDF_TMP, "zip");
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
				JDFServletUtil.cleanup(CHECK_JDF_TMP);
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
		resp.setContentType("text/xml;charset=utf-8");

		final PrintWriter out = resp.getWriter();
		out.println(xmlString);
		out.flush();
		out.close();
	}

	private JDFValidator getChecker(final boolean bUseSchema, final boolean bIgnorePrivate, final boolean prettyFormat, final String validationLevel, final File devcapFile)
	{
		final JDFValidator checker = new JDFValidator();

		checker.setPrint(false);
		checker.bQuiet = true;
		checker.setIgnorePrivate(bIgnorePrivate);
		checker.level = EnumValidationLevel.Complete;
		try
		{
			checker.level = EnumValidationLevel.getEnum(validationLevel);
		}
		catch (final Exception x)
		{
			// nop
		}
		if (prettyFormat)
			checker.xslStyleSheet = "./checkjdf.xsl";

		if (devcapFile != null && devcapFile.canRead())
		{
			checker.devCapFile = devcapFile.getAbsolutePath();
			log.info("Devcap file: " + devcapFile.getAbsolutePath());
		}

		if (bUseSchema) // using schema
		{
			checker.setJDFSchemaLocation("http://schema.cip4.org/jdfschema_1_7/JDF.xsd");
		}
		return checker;
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
		String uri = request.getRequestURI();
		uri = StringUtil.token(uri, 0, "?");
		final String last = StringUtil.token(uri, -1, JDFConstants.SLASH);
		final boolean isFix = last.startsWith(FIX_JDF) || StringUtil.hasToken(uri, FIX_JDF_TMP, JDFConstants.SLASH, 0);
		return isFix ? new FixCall(this, request, response) : new CheckJDFCall(this, request, response);
	}
}
