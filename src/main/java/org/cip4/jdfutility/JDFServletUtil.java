/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2013 The International Cooperation for the Integration of 
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.cip4.jdflib.util.PlatformUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

/**
 * 
 * @author rainer
 */
public class JDFServletUtil extends Object
{

	static long lastCleanup = 0;

	static int fileCounter = 1000;
	/**
	 * the current working directory
	 */
	final static public String cwd = System.getProperty("user.dir");
	/**
	 * the servlet base directory
	 */
	final static public String baseDir = System.getProperty("catalina.base") + "/webapps/JDFUtility/";

	/**
	 * @param dirName
	 * @param tmpName
	 * @param prefix
	 * @param extension
	 * @param bCatalina
	 * @return
	 */
	public static File getTmpFile(final String dirName, String tmpName, final String prefix, final String extension, final boolean bCatalina)
	{
		if (tmpName == null)
		{
			tmpName = "File" + fileCounter++;
		}

		// we are in bin, which is a sibling directory of JDFUtilitys
		final File tmpDir = new File(bCatalina ? baseDir + dirName : dirName);

		if (!tmpDir.isDirectory())
		{
			tmpDir.mkdirs();
		}

		final char[] tmp = tmpName.toCharArray();
		for (int i = 0; i < tmp.length; i++)
		{
			if ((tmp[i] > 127) || (tmp[i] <= 32))
			{
				tmp[i] = '_';
			}
		}
		tmpName = String.valueOf(tmp);
		tmpName = prefix + StringUtil.pathToName(tmpName);
		tmpName = UrlUtil.prefix(tmpName);

		File outFile;
		try
		{
			outFile = File.createTempFile(tmpName, extension, tmpDir);
		}
		catch (final IOException e)
		{
			return null;
		}
		return outFile;
	}

	/**
	 * @param dirName
	 * @param fileItem
	 * @param prefix
	 * @param extension
	 * @return
	 */
	public static File getTmpFile(final String dirName, final FileItem fileItem, final String prefix, final String extension)
	{
		if (fileItem == null)
		{
			return null;
		}
		final String tmpName = fileItem.getName();
		return getTmpFile(dirName, tmpName, prefix, extension, true);
	}

	/**
	 * @param request
	 * @return
	 * @throws ServletException
	 * @deprecated - use 4 parameter version
	 */
	@Deprecated
	public static List<FileItem> getFileList(final HttpServletRequest request) throws ServletException
	{
		FileItemList fil = new FileItemList(request, 20 * 1024 * 1024);
		return fil.getFileList(true, true);
	}

	/**
	 * cleanup previous junk that is older than an hour
	 * @param dirNam the directory to clean
	 */
	public static void cleanup(final String dirNam)
	{
		final long modNow = System.currentTimeMillis();
		if (modNow - lastCleanup < 600000)
			return; // only attempt cleanup every 10 minutes
		lastCleanup = modNow;
		final File tmpDir = new File(baseDir + dirNam);
		final File[] tmpFiles = tmpDir.listFiles();
		for (int n = 0; n < tmpFiles.length; n++)
		{
			final File oldFile = tmpFiles[n];
			if (modNow - 3600000 > oldFile.lastModified())
			{ // 3600 seconds timeout
				oldFile.delete();
				System.out.println("deleting " + oldFile.getName() + " " + String.valueOf(oldFile.lastModified()) + " " + String.valueOf(modNow));
			}
		}
		Runtime.getRuntime().gc(); // clean up memory
	}

	/**
	 * 
	 * @return
	 * @deprecated use {@link PlatformUtil}.isWindows()
	 *
	 */
	@Deprecated
	public static boolean isWindows()
	{
		return PlatformUtil.isWindows();
	}

}
