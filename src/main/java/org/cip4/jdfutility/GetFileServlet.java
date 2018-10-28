/*
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2018 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment mrSubRefay appear in the software itself, if and wherever such third-party
 * acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior writtenrestartProcesses() permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIrSubRefAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. ====================================================================
 *
 * } This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software restartProcesses() copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 */
package org.cip4.jdfutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.lf5.util.StreamUtils;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 *
 * @author rainer
 *
 *
 */
public class GetFileServlet extends HttpServlet
{

	private static Log log = LogFactory.getLog(GetFileServlet.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = -8902154436245089036L;
	private File baseDir = null;

	/**
	 * Initializes the servlet.
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException
	{
		super.init(config);
		final String root = config.getInitParameter("rootDir");
		log.info("Config root: " + root);
		baseDir = new File(root);
		baseDir.mkdir(); // create if it aint there
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
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 * @throws IOException
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		processRequest(request, response);
	}

	/**
	 * Parses a multipart request.
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		final OutputStream os = response.getOutputStream();
		final String localName = request.getPathInfo();
		final File f = FileUtil.getFileInDirectory(baseDir, new File(localName));
		if (f.exists())
		{
			response.setContentType(UrlUtil.getMimeTypeFromURL(localName));
			final FileInputStream input = new FileInputStream(f);
			StreamUtils.copyThenClose(input, response.getOutputStream());
		}
		else
		{
			response.setContentType(UrlUtil.TEXT_HTML);
			os.write("<HTML><H1>Error</H1><br/>Cannot find file: ".getBytes());
			os.write(localName.getBytes());
			os.write("</HTML>".getBytes());
		}
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "GETFile Servlet";
	}

}
