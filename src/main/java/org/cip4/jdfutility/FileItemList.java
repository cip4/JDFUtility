/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of 
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.DiskFileItemFactory.Builder;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.ContainerUtil;
import org.cip4.jdflib.util.StreamUtil;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.UrlUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author rainer
 */
public class FileItemList
{

	final private List<DiskFileItem> fileItems;
	private final JDFAttributeMap mapCache;
	private final JDFAttributeMap reqParameters;

	/**
	 * returns a pure in memory FileItemList
	 * 
	 * @param request
	 * @param filesize
	 * @return
	 */
	public static FileItemList getMemoryFileItemList(final HttpServletRequest request, final long filesize)
	{
		try
		{
			return new FileItemList(request, filesize, true);
		}
		catch (final ServletException e)
		{
			return null;
		}
	}

	/**
	 * 
	 * @param request
	 * @param filesize
	 * @return
	 */
	public static FileItemList getFileItemList(final HttpServletRequest request, final long filesize)
	{
		try
		{
			return new FileItemList(request, filesize, false);
		}
		catch (final ServletException e)
		{
			return null;
		}
	}

	public FileItemList(final HttpServletRequest request, final long filesize) throws ServletException
	{
		this(request, filesize, false);
	}

	/**
	 * 
	 * @param request
	 * @param filesize
	 * @throws ServletException
	 */
	FileItemList(final HttpServletRequest request, final long filesize, final boolean inMemory) throws ServletException
	{
		mapCache = new JDFAttributeMap();
		reqParameters = new JDFAttributeMap();
		fileItems = new ArrayList<DiskFileItem>();
		// Create a factory for disk-based file items
		final DiskFileItemFactory factory = getFactory(inMemory, filesize);

		// Create a new file upload handler
		final JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload = new JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory>();
		upload.setFileItemFactory(factory);
		if (request != null)
		{
			final Map<String, String[]> parameterMap = request.getParameterMap();
			if (parameterMap != null)
			{
				final Set<String> keySet = parameterMap.keySet();
				for (final String key : keySet)
				{
					final String[] vals = parameterMap.get(key);
					if (vals != null && vals.length > 0)
					{
						reqParameters.put(key, vals[0]);
					}
				}
			}
			if (UrlUtil.POST.equalsIgnoreCase(request.getMethod()))
			{
				try
				{
					if (filesize > 0)
						upload.setSizeMax(filesize);
					ContainerUtil.addAll(fileItems, upload.parseRequest(request));
				}
				catch (final FileUploadException fue)
				{
					throw new ServletException("Could not parse multipart request.", fue);
				}
			}
		}
	}

	DiskFileItemFactory getFactory(final boolean inMemory, final long filesize)
	{
		if (inMemory && filesize <= 1)
		{
			throw new IllegalArgumentException("cannot create in memory factory with low size " + filesize);
		}
		final Builder b = DiskFileItemFactory.builder();
		if (inMemory)
		{
			b.setBufferSize((int) filesize);
		}
		return b.get();
	}

	/**
	 * 
	 * 
	 * @return
	 * 
	 */
	public JDFAttributeMap getFieldsFromForm()
	{
		if (ContainerUtil.isEmpty(mapCache))
		{
			final List<DiskFileItem> fileList = getFileList(false, true);
			for (final DiskFileItem fi : fileList)
			{
				try
				{
					final String itemString = StringUtil.getNonEmpty(fi.getString());
					if (itemString != null)
					{
						mapCache.put(fi.getFieldName(), itemString);
					}
				}
				catch (final Exception e)
				{
					// nop
				}
			}

			mapCache.putAll(reqParameters);
		}
		return mapCache;
	}

	/**
	 * 
	 * get a form value
	 * 
	 * @param key
	 * @return
	 */
	public String getField(final String key)
	{
		getFieldsFromForm();
		return mapCache.getIgnoreCase(key);
	}

	/**
	 * 
	 * get a form value
	 * 
	 * @param key
	 * @param def
	 * @return
	 */
	public int getIntField(final String key, final int def)
	{
		getFieldsFromForm();
		return StringUtil.parseInt(mapCache.getIgnoreCase(key), def);
	}

	/**
	 * 
	 * get a form value
	 * 
	 * @param key
	 * @param def
	 * @return
	 */
	public boolean getBoolField(final String key, final boolean def)
	{
		getFieldsFromForm();
		return StringUtil.parseBoolean(mapCache.getIgnoreCase(key), def);
	}

	/**
	 * @param bFile if true return files
	 * @param bForm if true return form fields
	 * @return
	 * 
	 */
	public List<DiskFileItem> getFileList(final boolean bFile, final boolean bForm)
	{
		final List<DiskFileItem> retList = new ArrayList<DiskFileItem>();
		if (bFile || bForm)
		{
			for (final DiskFileItem f : fileItems)
			{
				final boolean formField = f.isFormField();
				if (formField && bForm || !formField && bFile)
				{
					retList.add(f);
				}
			}
		}
		return retList;
	}

	/**
	 * 
	 * get the iTh file
	 * 
	 * @param i may be<0 to count from end
	 * @return
	 */
	public DiskFileItem getFile(int i)
	{
		final List<DiskFileItem> fileList = getFileList(true, false);
		if (i < 0)
		{
			i += fileList.size();
		}
		if (i < 0 || i >= fileList.size())
		{
			return null;
		}
		else
		{
			return fileList.get(i);
		}
	}

	/**
	 * 
	 * get the file attached to formName
	 * 
	 * @param formName
	 * @return
	 */
	public DiskFileItem getFile(final String formName)
	{
		final List<DiskFileItem> fileList = getFileList(true, false);
		for (final DiskFileItem f : fileList)
		{
			if (f.getFieldName().equalsIgnoreCase(formName))
			{
				return f;
			}
		}
		return null;
	}

	/**
	 * 
	 * get the input stream for formName
	 * 
	 * @param formName
	 * @return
	 */
	public InputStream getFileInputStream(final String formName)
	{
		final DiskFileItem fi = getFile(formName);
		return getInputStream(fi);
	}

	/**
	 * 
	 * get the input stream for formName
	 * 
	 * @param i may be<0 to count from end
	 * @return
	 */
	public InputStream getFileInputStream(final int i)
	{
		final DiskFileItem fi = getFile(i);
		return getInputStream(fi);
	}

	/**
	 * 
	 * 
	 * @param fi
	 * @return
	 */
	public static InputStream getInputStream(final DiskFileItem fi)
	{
		if (fi != null)
		{
			InputStream inputStream;
			try
			{
				inputStream = fi.getInputStream();
			}
			catch (final IOException e)
			{
				inputStream = null;
			}
			return StreamUtil.getBufferedInputStream(inputStream);
		}
		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "FileItemList [mapCache=" + mapCache.getKeyList().getString() + ", fileItems=" + fileItems + "]";
	}

}
