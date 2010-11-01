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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.util.FastFiFo;
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.StringUtil;

/**
 * base servlet class
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public abstract class UtilityServlet extends HttpServlet
{
	protected class RequestStats
	{
		/**
		 * 
		 * @param method
		 * @param url
		 */
		public RequestStats(String method, String url)
		{
			super();
			this.method = method;
			this.url = url;
			timeStamp = System.currentTimeMillis();
		}

		/**
		 * 
		 * @param request the request
		 */
		public RequestStats(HttpServletRequest request)
		{
			super();
			this.method = request.getMethod();
			this.url = getRequestURL(request);
			timeStamp = System.currentTimeMillis();
		}

		private final String url;
		private final long timeStamp;
		private final String method;

		private String getRequestURL(HttpServletRequest request)
		{
			String params = StringUtil.getNonEmpty(request.getQueryString());
			String url = request.getRequestURL().toString();
			if (params != null)
				url += "?" + params;
			return url;
		}

		public String getUrl()
		{
			return url;
		}

		public long getTimeStamp()
		{
			return timeStamp;
		}

		public String getMethod()
		{
			return method;
		}

		/**
		 * get the row of values for this (time method url)
		 * @return the row
		 */
		public VString getRow()
		{
			VString v = new VString();
			v.add(new JDFDate(getTimeStamp()).getTimeISO());
			v.add(getMethod());
			v.add(getUrl());
			return v;
		}
	}

	/**
	 * 
	 */
	public UtilityServlet()
	{
		super();
		log = LogFactory.getLog(getClass());
		startDate = new JDFDate();
		numGet = 0;
		numPost = 0;
		tTotal = 0;
		tCPUTotal = 0;
		tMax = 0;
		tCPUMax = 0;
		fifo = new FastFiFo<UtilityServlet.RequestStats>(50);

		setCssURL("http://www.cip4.org/css/styles_pc.css");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6858617993170970143L;
	protected long numGet;
	protected long numPost;
	protected Log log;
	private String cssURL;
	protected long tTotal;
	protected long tCPUTotal;
	protected long tMax;
	protected long tCPUMax;
	protected long requestLen;
	protected final JDFDate startDate;
	FastFiFo<RequestStats> fifo;

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	final protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		ServletCall si = prepareGet(request, response);
		si.processGet();
		si.finalizeGet();
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	final protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		ServletCall si = preparePost(request, response);
		si.processPost();
		si.finalizePost();
	}

	/**
	 * @param request
	 * @param response
	 * @return 
	 */
	protected ServletCall preparePost(HttpServletRequest request, HttpServletResponse response)
	{
		numPost++;
		ServletCall servletInfo = getServletCall(request, response);
		log.info("Processing Post request# " + numPost + " / " + (numGet + numPost) + " for " + getServletInfo() + " request id=" + servletInfo.id);
		return servletInfo;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 */
	protected abstract ServletCall getServletCall(HttpServletRequest request, HttpServletResponse response);

	/**
	 * @param request
	 * @param response
	 * @return 
	 */
	protected ServletCall prepareGet(HttpServletRequest request, HttpServletResponse response)
	{
		numGet++;
		ServletCall servletInfo = getServletCall(request, response);
		log.info("Processing Get request# " + numGet + " / " + (numGet + numPost) + " for " + getServletInfo() + " request id=" + servletInfo.id);
		servletInfo.setupGet(request, response);

		return servletInfo;
	}

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

	/**
	 * @param cssURL the cssURL to set
	 */
	public void setCssURL(String cssURL)
	{
		this.cssURL = cssURL;
	}

	/**
	 * @param request 
	 * @return the cssURL
	 */
	public String getCssURL(HttpServletRequest request)
	{
		String url = request.getRequestURL().toString();
		int nTokens = StringUtil.tokenize(url, "/", false).size();
		String prefix = "";
		for (int i = 4; i < nTokens; i++)
			prefix += "../";
		return prefix + cssURL;
	}

	/**
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @throws ServletException
	 * @throws IOException
	*/
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		fifo.push(new RequestStats(request));
		int contentLength = request.getContentLength();
		if (contentLength > 0)
			requestLen += contentLength;
		super.service(request, response);
	}

	// //////////////////////////////////////////////////////////////////

}
