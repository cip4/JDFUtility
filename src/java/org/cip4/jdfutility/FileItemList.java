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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.util.StreamUtil;
import org.cip4.jdflib.util.StringUtil;

/**
 * 
 * @author rainer
 */
public class FileItemList
{

	final private List<FileItem> fileItems;
	private JDFAttributeMap mapCache;
	private JDFAttributeMap reqParameters;

	/**
	 *  
	 * @param request
	 * @param filesize
	 * @return
	 */
	public static FileItemList getFileItemList(HttpServletRequest request, long filesize)
	{
		try
		{
			return new FileItemList(request, filesize);
		}
		catch (ServletException e)
		{
			return null;
		}
	}

	/**
	 * 
	 * @param request
	 * @param filesize
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	public FileItemList(HttpServletRequest request, long filesize) throws ServletException
	{
		mapCache = null;
		// Create a factory for disk-based file items
		final FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		final ServletFileUpload upload = new ServletFileUpload(factory);
		if (request == null)
		{
			fileItems = null;
			reqParameters = null;
		}
		else
		{
			reqParameters = new JDFAttributeMap();
			Map<String, String> parameterMap = request.getParameterMap();
			if (parameterMap != null)
			{
				reqParameters.putAll(parameterMap);
			}
			try
			{
				if (filesize > 0)
					upload.setSizeMax(filesize);
				fileItems = upload.parseRequest(request);
			}
			catch (final FileUploadException fue)
			{
				throw new ServletException("Could not parse multipart request.", fue);
			}
		}
	}

	/**
	 * 
	 *  
	 * @return
	 *  
	 */
	public JDFAttributeMap getFieldsFromForm()
	{
		if (mapCache == null)
		{
			mapCache = new JDFAttributeMap();
			List<FileItem> fileList = getFileList(false, true);
			if (fileList != null)
			{
				for (FileItem fi : fileList)
				{
					String itemString = StringUtil.getNonEmpty(fi.getString());
					if (itemString != null)
					{
						mapCache.put(fi.getFieldName(), itemString);
					}
				}
			}
			mapCache.putAll(reqParameters);
		}
		return mapCache;
	}

	/**
	 * 
	 * get a form value
	 * @param key
	 * @return
	 */
	public String getField(String key)
	{
		getFieldsFromForm();
		return mapCache.get(key);
	}

	/**
	 * 
	 * get a form value
	 * @param key
	 * @param def
	 * @return
	 */
	public int getIntField(String key, int def)
	{
		getFieldsFromForm();
		return mapCache.getInt(key, def);
	}

	/**
	 * 
	 * get a form value
	 * @param key
	 * @param def
	 * @return
	 */
	public boolean getBoolField(String key, boolean def)
	{
		getFieldsFromForm();
		return mapCache.getBool(key, def);
	}

	/**
	 * @param bFile if true return files 
	 * @param bForm if true return form fields
	 * @return
	 *  
	 */
	public List<FileItem> getFileList(boolean bFile, boolean bForm)
	{
		List<FileItem> retList = new Vector<FileItem>();
		if ((bFile || bForm) && fileItems != null)
		{
			for (FileItem f : fileItems)
			{
				boolean formField = f.isFormField();
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
	 * @param i may be<0 to count from end
	 * @return
	 */
	public FileItem getFile(int i)
	{
		List<FileItem> fileList = getFileList(true, false);
		if (fileList != null)
		{
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
		return null;
	}

	/**
	 * 
	 * get the file attached to formName
	 * @param formName 
	 * @return
	 */
	public FileItem getFile(String formName)
	{
		List<FileItem> fileList = getFileList(true, false);
		if (fileList != null)
		{
			for (FileItem f : fileList)
			{
				if (f.getFieldName().equals(formName))
				{
					return f;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * get the input stream for formName
	 * @param formName 
	 * @return
	 */
	public InputStream getFileInputStream(String formName)
	{
		FileItem fi = getFile(formName);
		return getInputStream(fi);
	}

	/**
	 * 
	 * get the input stream for formName
	 * @param i may be<0 to count from end
	 * @return
	 */
	public InputStream getFileInputStream(int i)
	{
		FileItem fi = getFile(i);
		return getInputStream(fi);
	}

	/**
	 * 
	 *  
	 * @param fi
	 * @return
	 */
	public static InputStream getInputStream(FileItem fi)
	{
		if (fi != null)
		{
			InputStream inputStream;
			try
			{
				inputStream = fi.getInputStream();
			}
			catch (IOException e)
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
		return "FileItemList [mapCache=" + mapCache + ", fileItems=" + fileItems + "]";
	}

}
