/*
 *
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 *
 * @author rainer
 *
 *         very trivial http to file servlet, see web.xml for setup parameters
 */
public class JMFHotFolderServlet extends HttpServlet
{

	private File baseDir = null;
	private static int index = 0;
	private static Object mutexInc = new Object();
	private static Object mutexDel = new Object();
	private int maxKeep = 1000;
	private Set<String> mimeTypes = null;
	/**
	 *
	 */
	private static final long serialVersionUID = -8902151736245089036L;

	private static int increment()
	{
		synchronized (mutexInc)
		{
			return index++;
		}
	}

	/**
	 * Initializes the servlet.
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException
	{
		super.init(config);
		final String root = config.getInitParameter("rootDir");
		System.out.println("Config root: " + root);
		baseDir = new File(root);
		final String rootBak = root + ".bak";
		final File fBak = new File(rootBak);
		FileUtil.deleteAll(fBak);
		final String zapp = config.getInitParameter("flushOnInit");
		if (StringUtil.parseBoolean(zapp, false))
		{
			if (baseDir.isDirectory())
				FileUtil.deleteAll(baseDir);
		}
		else
		{
			if (baseDir.isDirectory())
				baseDir.renameTo(fBak);
		}
		baseDir.mkdir(); // create if it aint there
		final String sMaxKeep = config.getInitParameter("maxKeep");
		maxKeep = StringUtil.parseInt(sMaxKeep, 1000);
		final String sMimeTypes = config.getInitParameter("mimeTypes");
		if (sMimeTypes != null)
		{
			final VString vTypes = StringUtil.tokenize(sMimeTypes, null, false);
			if (!vTypes.contains(JDFConstants.STAR))
			{
				mimeTypes = vTypes.getSet();
			}
		}
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
	 * Handles all HTTP <code>POST</code> methods.
	 * 
	 * @param request servlet request
	 * @param response servlet response
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (mimeTypes != null)
		{
			final String mimeType = request.getContentType();
			if (!mimeTypes.contains(mimeType))
				return; // non of our business
		}
		final int inc = increment();
		if (inc % 100 == 0)
			System.out.println("jmf dump service " + index);

		final String s = StringUtil.sprintf("m%08d.jmf", "" + inc);
		final File f = FileUtil.getFileInDirectory(baseDir, new File(s));
		try
		{
			FileUtil.streamToFile(request.getInputStream(), f);
		}
		catch (final IOException e)
		{
			// nop
		}
		cleanup(inc);
	}

	/**
	 * @param inc
	 */
	private void cleanup(final int inc)
	{
		if (inc % 100 == 0)
		{
			synchronized (mutexDel)
			{
				final String[] names = baseDir.list();
				if (names.length > maxKeep)
				{
					Arrays.sort(names);
					for (int i = 0; i < names.length - maxKeep; i++)
					{
						File f = new File(names[i]);
						f = FileUtil.getFileInDirectory(baseDir, f);
						f.delete();
					}
				}
			}
		}
	}

	/**
	 * Returns a short description of the servlet.
	 */
	@Override
	public String getServletInfo()
	{
		return "JMFHotFolderServlet";
	}

}
