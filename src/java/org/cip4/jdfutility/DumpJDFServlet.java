/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2012 The International Cooperation for the Integration of 
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.zip.ZipReader;
import org.cip4.jdfutility.html.HTMLUtil;

/**
 * 
 * @author rainer
 * 
 * 
 */
public class DumpJDFServlet extends UtilityServlet
{
	protected class DumpCall extends ServletCall
	{
		/**
		 * @param utilityServlet
		 * @param request
		 * @param response
		 */
		public DumpCall(UtilityServlet utilityServlet, HttpServletRequest request, HttpServletResponse response)
		{
			super(utilityServlet, request, response);
		}

		/**
		 * Handles all HTTP <code>GET / POST etc.</code> methods.
		 */
		@Override
		protected void processGet()
		{
			String error = updateProxy();
			final String dir = request.getPathInfo();
			File newDir = dir == null ? baseDir.getDir() : FileUtil.getFileInDirectory(baseDir.getDir(), new File(dir));
			ByteArrayIOStream bos = dumpToFile();
			KElement body = getHTMLRoot().getCreateElement("body");
			HTMLUtil.appendHeader(body, 1, "Dump HTML");
			HTMLUtil.appendLine(body, "Dump Directory: " + newDir);
			String header = getRequestURL();
			HTMLUtil.appendLine(body, header);
			if (error != null)
			{
				HTMLUtil.appendHeader(body, 3, "Error updating Proxy: ");
			}
			String displayProxy = proxyURL == null ? "" : proxyURL;

			proxyForm(body, displayProxy);
			printHistory(body);

			body.appendElement("h2").setText("Summary");
			HTMLUtil.appendLine(body, "# Total Forwards: " + (numForward + numBadForward));
			HTMLUtil.appendLine(body, "# Successfull Forwards: " + numForward);
			HTMLUtil.appendLine(body, "# Failed Forwards: " + numBadForward);

			forward(bos, request);
			System.gc();
		}

		private void printHistory(KElement body)
		{
			body.appendElement("h2").setText("History");
			KElement table = HTMLUtil.appendTable(body, new VString("Time Method URL", null));
			RequestStats[] stats = fifo.peekArray();
			if (stats != null)
			{
				for (int i = stats.length - 1; i >= 0; i--)
				{
					HTMLUtil.appendTableRow(table, stats[i].getRow());
				}
			}
		}

		private void proxyForm(KElement body, String displayProxy)
		{
			body.appendElement("h2").setText("proxy url");
			KElement form = body.appendElement("form");
			form.setAttribute("action", request.getContextPath());
			KElement input = form.appendElement("input");
			input.setAttribute("type", "text");
			input.setAttribute("name", "proxy");
			input.setAttribute("size", "60");
			input.setAttribute("value", displayProxy);

			input = form.appendElement("input");
			input.setAttribute("type", "submit");
			input.setAttribute("name", "submit");
			input.setAttribute("value", "update proxy");
		}

		protected String getRequestURL()
		{
			String params = StringUtil.getNonEmpty(request.getQueryString());
			String header = "Context Path: " + request.getRequestURL().toString();
			if (params != null)
				header += "?" + params;
			return header;
		}

		/**
		 * Handles all HTTP <code>GET / POST etc.</code> methods.
		 */
		@Override
		protected void processPost()
		{
			// System.out.println("dump service");
			final String nodump = request.getParameter("nodump");
			final boolean dump = !StringUtil.parseBoolean(nodump, false);
			ByteArrayIOStream bos = null;
			if (dump)
			{
				bos = dumpToFile();
			}
			else
			{
				try
				{
					if (proxyURL != null)
					{
						bos = new ByteArrayIOStream(request.getInputStream());
					}
					final OutputStream os = response.getOutputStream();
					final PrintWriter w = new PrintWriter(os);
					w.print("<HTML><HEAD><TITLE>JDF Test DUMP</TITLE></HEAD></HTML>");
					w.flush();
				}
				catch (final Exception e)
				{
					log.error("whazzup? ", e);
				}
			}
			forward(bos, request);

		}

		/**
		 * @return the error string, null if ok
		 */
		private String updateProxy()
		{
			String newProxy = StringUtil.getNonEmpty(request.getParameter("proxy"));
			String error = null;
			if (newProxy != null)
			{
				try
				{
					URL url = new URL(newProxy);
					boolean same = true;
					int port = request.getLocalPort();
					int urlPort = url.getPort();
					same = same && port == urlPort;
					String server = request.getLocalName();
					String urlServer = url.getHost();
					same = same && ContainerUtil.equals(server.toLowerCase(), urlServer.toLowerCase());
					String path = StringUtil.token(request.getContextPath(), 0, "/");
					String urlPath = StringUtil.token(url.getPath(), 0, "/");
					same = same && ContainerUtil.equals(path.toLowerCase(), urlPath.toLowerCase());

					if (!same)
						proxyURL = url.toExternalForm();
					else
						error = "cannot create proxy for self - infinite loop<br/>";
				}
				catch (MalformedURLException x)
				{
					proxyURL = null;
					error = x.toString() + "<br/>";
				}
			}
			return error;
		}

		/**
		 * @return 
		 */
		private ByteArrayIOStream dumpToFile()
		{
			final String dir = request.getPathInfo();
			File newDir = dir == null ? baseDir.getDir() : FileUtil.getFileInDirectory(baseDir.getDir(), new File(dir));
			if (newDir.exists() && !newDir.isDirectory())
			{
				newDir = baseDir.getDir();
			}
			else
			{
				newDir.mkdirs();
			}
			final DumpDir theDump = getCreateDump(newDir);
			String header = getRequestURL();
			contentLength = request.getContentLength();
			header += "\nmethod: " + request.getMethod();
			header += "\nContent Length: " + contentLength;
			header += "\nRemote host: " + request.getRemoteHost() + ":" + request.getRemotePort();

			@SuppressWarnings("unchecked")
			Enumeration<String> set = request.getHeaderNames();
			if (set != null)
			{
				header += "\nHTTP Header List: \n";
				while (set.hasMoreElements())
				{
					String key = set.nextElement();
					header += "  " + key + "=" + request.getHeader(key) + "\n";
				}
			}
			ByteArrayIOStream bos = null;
			try
			{
				bos = new ByteArrayIOStream(request.getInputStream());

				final File f = theDump.newFileFromStream(header, bos.getInputStream(), null);
				if (contentLength < 0)
				{
					final FileInputStream fis = new FileInputStream(f);
					contentLength = fis.available() - header.length() - 28;
				}
				requestLen += contentLength;
				final String contentType = request.getContentType();
				if (contentType != null && contentType.toLowerCase().startsWith("multipart/related"))
				{
					dumpMime(bos, f.getPath());
				}
				else if (UrlUtil.APPLICATION_ZIP.equalsIgnoreCase(contentType))
				{
					dumpZip(bos, f.getPath());
				}
			}
			catch (final Exception e)
			{
				log.error("dump service - snafu: ", e);
			}
			System.gc();
			return bos;
		}

		protected void dumpMime(final ByteArrayIOStream bos, String path) throws FileNotFoundException, IOException, MessagingException
		{
			final InputStream fis = bos.getInputStream();
			final String dirName = UrlUtil.newExtension(path, "mime.dir");
			log.info("dump mime: " + dirName);

			final Multipart mp = MimeUtil.getMultiPart(fis);
			MimeUtil.writeToDir(mp, new File(dirName));
		}

		protected void dumpZip(final ByteArrayIOStream bos, String path) throws FileNotFoundException, IOException, MessagingException
		{
			final InputStream fis = bos.getInputStream();
			final String dirName = UrlUtil.newExtension(path, "zip.dir");
			log.info("dump zip: " + dirName);
			//			FileUtil.streamToFile(fis, dirName + ".zip");
			ZipReader zip = new ZipReader(fis);
			zip.unPack(new File(dirName));
		}

		protected void skipHeader(final InputStream fis) throws IOException
		{
			char c = 'a';
			while (c != '!')
			{
				c = (char) fis.read();
			}
			c = (char) fis.read();
		}

		/**
		 * @param newDir
		 * @return 
		 */
		private DumpDir getCreateDump(final File newDir)
		{
			synchronized (subDumps)
			{
				DumpDir theDump = subDumps.get(newDir);
				if (theDump == null)
				{
					log.info("creating new Directory: " + newDir.getPath());
					theDump = new DumpDir(newDir);
					theDump.quiet = false;

					subDumps.put(newDir, theDump);
				}
				return theDump;
			}
		}

		/**
		 * @param bos
		 * @param req 
		 */
		private void forward(ByteArrayIOStream bos, HttpServletRequest req)
		{
			if (bos == null || req == null || proxyURL == null)
				return;

			try
			{
				final URL url = new URL(proxyURL);

				HttpURLConnection httpURLconnection = (HttpURLConnection) url.openConnection();
				String method = req.getMethod();
				httpURLconnection.setRequestMethod(method);
				httpURLconnection.setRequestProperty("Connection", "close");
				if (!UrlUtil.GET.equalsIgnoreCase(method))
				{
					String contentType = req.getContentType();
					httpURLconnection.setRequestProperty(UrlUtil.CONTENT_TYPE, contentType);
					httpURLconnection.setDoOutput(true);
					final OutputStream out = httpURLconnection.getOutputStream();
					IOUtils.copy(bos.getInputStream(), out);
					out.flush();
					out.close();
				}
				int rc = httpURLconnection.getResponseCode(); // close channel
				if (rc == 200)
					numForward++;
				else
					numBadForward++;
			}
			catch (Exception x)
			{
				// nop
			}

		}
	}

	/**
	 * 
	 */
	public DumpJDFServlet()
	{
		super();
		baseDir = null;
		subDumps = new HashMap<File, DumpDir>();
		numBadForward = 0;
		numForward = 0;
	}

	protected DumpDir baseDir;
	protected final HashMap<File, DumpDir> subDumps;
	protected int numBadForward;
	protected int numForward;
	protected String proxyURL;
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
		proxyURL = null;
		final String root = config.getInitParameter("rootDir");
		log.info("Config root: " + root);
		final File rootFile = new File(root);
		baseDir = new DumpDir(rootFile);
		baseDir.quiet = false;
		subDumps.put(rootFile, baseDir);
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "DumpJDF Servlet";
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
		return new DumpCall(this, request, response);
	}

}
