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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import org.cip4.jdflib.util.ContainerUtil;
import org.junit.jupiter.api.Test;

class FileItemListTest extends JDFUtilityTestBase
{

	@Test
	void testString() throws Exception
	{
		assertNotNull(new FileItemList(getRequestMock(), 42).toString());
	}

	@Test
	void testGetBool() throws Exception
	{
		assertFalse(new FileItemList(getRequestMock(), 42).getBoolField("a", false));
		assertTrue(new FileItemList(getRequestMock(), 42).getBoolField("a1", false));
		assertTrue(new FileItemList(getRequestMock(), 42).getBoolField("A1", false));
	}

	@Test
	void testGetInt() throws Exception
	{
		assertEquals(0, new FileItemList(getRequestMock(), 42).getIntField("a", 0));
		assertEquals(1, new FileItemList(getRequestMock(), 42).getIntField("a2", 0));
		assertEquals(1, new FileItemList(getRequestMock(), 42).getIntField("A2", 0));
	}

	@Test
	void testGetField() throws Exception
	{
		assertEquals(null, new FileItemList(getRequestMock(), 42).getField("a"));
		assertEquals("abc", new FileItemList(getRequestMock(), 42).getField("a3"));
		assertEquals("abc", new FileItemList(getRequestMock(), 42).getField("A3"));
	}

	@Test
	void testGetFile() throws Exception
	{
		assertNull(new FileItemList(getRequestMock(), 42).getFile(0));
		assertNull(new FileItemList(getRequestMock(), 42).getFile("foo"));
	}

	@Test
	void testGetFileStream() throws Exception
	{
		assertNull(new FileItemList(getRequestMock(), 42).getFileInputStream(0));
		assertNull(new FileItemList(getRequestMock(), 42).getFileInputStream("foo"));
	}

	@Test
	void testGetMem() throws Exception
	{
		assertNull(FileItemList.getMemoryFileItemList(getRequestMock(), 999).getFileInputStream(0));
		assertNull(FileItemList.getFileItemList(getRequestMock(), 999).getFileInputStream(0));
	}

	HttpServletRequest getRequestMock() throws IOException, ServletException
	{
		final HttpServletRequest mock = mock(HttpServletRequest.class);
		final Map<String, String[]> pm = new HashMap<String, String[]>();
		pm.put("A1", new String[] { "true" });
		pm.put("A2", new String[] { "1" });
		pm.put("A3", new String[] { "abc" });
		when(mock.getParameterMap()).thenReturn(pm);

		final Part part1 = mock(Part.class);
		when(mock.getPart(any())).thenReturn(part1);
		when(mock.getParts()).thenReturn(ContainerUtil.add(new ArrayList<Part>(), part1));
		return mock;
	}

	@Test
	void testGetFactory() throws Exception
	{
		final FileItemList l = new FileItemList(getRequestMock(), 42);
		assertThrows(IllegalArgumentException.class, () -> l.getFactory(true, 0));
		l.getFactory(false, 0);
		l.getFactory(false, 99999);
		l.getFactory(true, 99999);
	}

}
