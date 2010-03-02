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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.JDFDate;

/**
 * base servlet class
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public abstract class UtilityServlet extends HttpServlet
{
	/**
	 * 
	 */
	public UtilityServlet()
	{
		super();
		log = LogFactory.getLog(getClass());
		startDate = new JDFDate();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6858617993170970143L;
	protected long numGet = 0;
	protected long numPost = 0;
	protected Log log;
	protected static long tTotal = 0;
	protected static long tCPUTotal = 0;
	protected long requestLen;
	protected final JDFDate startDate;

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	final protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		ServletInfo si = prepareGet(request, response);
		si.w = processGet(request, response);
		finalizeGet(request, response, si);
	}

	/**
	 * bookkeeping 
	  * @author Rainer Prosi, Heidelberger Druckmaschinen *
	 */
	protected class ServletInfo
	{
		public ServletInfo(final HttpServletRequest request)
		{
			super();
			tStart = System.currentTimeMillis();
			id = KElement.uniqueID(0);
			contentLength = request.getContentLength();
			if (contentLength > 0)
				requestLen += contentLength;
			timer = new CPUTimer(true);

		}

		/**
		 * 
		 * @return
		 */
		public long getTimeProcessed()
		{
			long deltaT = System.currentTimeMillis() - tStart;
			return deltaT;
		}

		/**
		 * 
		 */
		public void complete()
		{
			tTotal += getTimeProcessed();
			long t = timer.getCPUTime();
			if (t > 0)
				tCPUTotal += (t / 1000); // micros is ok
		}

		public long tStart;
		public CPUTimer timer;
		String id;
		PrintWriter w;
		int contentLength;

	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	final protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		ServletInfo si = preparePost(request, response);
		processPost(request, response);
		finalizePost(request, response, si);
	}

	/**
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	protected abstract void processPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

	/**
	 * @param request
	 * @param response
	 * @return 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	protected PrintWriter processGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		final PrintWriter w = setupGet(request, response);
		return w;
	}

	/**
	 * @param request
	 * @param response
	 * @param si
	 */
	protected void finalizePost(HttpServletRequest request, HttpServletResponse response, ServletInfo si)
	{
		long deltaT = si.getTimeProcessed();
		si.complete();
		log.info("Processed Post " + getServletInfo() + " request. ID=" + si.id + " Time required:" + deltaT + " Total time:" + tTotal);
	}

	/**
	 * @param request
	 * @param response
	 * @param si
	 */
	protected void finalizeGet(HttpServletRequest request, HttpServletResponse response, ServletInfo si)
	{
		long deltaT = si.getTimeProcessed();
		si.complete();
		if (si.w != null)
		{
			si.w.println("Time Spent (milliSeconds): " + deltaT + " Total time(seconds): " + tTotal / 1000. + " Average: " + (tTotal / (numGet + numPost)) + "<BR/>");
			si.w.println("CPU Time Spent (milliSeconds): " + si.timer.getCPUTime() / 1000000. + " Total CPU time (seconds): " + tCPUTotal / 1000000. + " Average: "
					+ (tCPUTotal * 0.001 / (numGet + numPost)) + "<BR/><BR/>");
			si.w.println("<HR/>" + new JDFDate().getFormattedDateTime("MMM' 'dd' 'yyyy' - 'HH:mm:ss"));
			si.w.print("<font size='-1' color='gray'> - active since: " + startDate.getFormattedDateTime("MMM' 'dd' 'yyyy' - 'HH:mm:ss") + "</font></Body></HTML>");

			si.w.flush();
		}
		else
		{
			log.error("no output print writer, bailing out");
		}
		log.info("Processed Get " + getServletInfo() + " request. ID=" + si.id + " Time required:" + deltaT + " Total time:" + tTotal);
	}

	/**
	 * @param request
	 * @param response
	 * @return 
	 */
	protected ServletInfo preparePost(HttpServletRequest request, HttpServletResponse response)
	{
		numPost++;
		ServletInfo servletInfo = new ServletInfo(request);
		log.info("Processing Post request# " + numPost + " / " + (numGet + numPost) + " for " + getServletInfo() + " request id=" + servletInfo.id);
		return servletInfo;
	}

	/**
	 * @param request
	 * @param response
	 * @return 
	 */
	protected ServletInfo prepareGet(HttpServletRequest request, HttpServletResponse response)
	{
		numGet++;
		ServletInfo servletInfo = new ServletInfo(request);
		log.info("Processing Get request# " + numGet + " / " + (numGet + numPost) + " for " + getServletInfo() + " request id=" + servletInfo.id);
		return servletInfo;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	protected PrintWriter setupGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		final String contentType = request.getContentType();
		String rHost = request.getRemoteHost();
		int rPort = request.getRemotePort();
		int contentLength = request.getContentLength();
		final OutputStream os = response.getOutputStream();
		final PrintWriter w = new PrintWriter(os);
		w.print("<HTML><HEAD><TITLE>" + getServletInfo() + "</TITLE></HEAD>");
		w.print("<Body><H1>Request Dump</H1><HR/>");
		w.println("<h2>this request	</h2>");
		w.println("Content Type: " + contentType + "<BR/>");
		w.println("Content Length: " + contentLength + "<BR/>");
		w.println("Request URI: " + request.getRequestURL().toString() + "<BR/>");
		w.println("Remote host: " + rHost + ":" + rPort + "<BR/>");
		w.println("# Get requests: " + numGet + "<BR/>");
		w.println("# Post requests: " + numPost + "<BR/>");
		w.println("# Bytes Processed: " + requestLen + "<BR/>");
		w.println("Free Memory: " + Runtime.getRuntime().freeMemory() + "<BR/>");
		w.println("Total Memory: " + Runtime.getRuntime().totalMemory() + "<BR/>");
		return w;
	}

	// //////////////////////////////////////////////////////////////////

	protected File createTmpFile(final FileItem fileItem, final String baseDir, final String ext) throws FileNotFoundException, IOException
	{
		final InputStream s = fileItem.getInputStream();
		final File zipFile = JDFServletUtil.getTmpFile(baseDir, fileItem, ext + "_", "." + ext);
		final FileOutputStream fos = new FileOutputStream(zipFile);
		final int n = IOUtils.copy(s, fos);
		fos.flush();
		fos.close();
		System.out.println(ext + " file name: " + zipFile.toString() + " size: " + n);
		return zipFile;
	}

	// //////////////////////////////////////////////////////////////////

}
