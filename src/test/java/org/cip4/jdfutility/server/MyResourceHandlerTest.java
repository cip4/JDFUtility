/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
package org.cip4.jdfutility.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cip4.jdfutility.JDFUtilityTestBase;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Context;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.Test;

class MyResourceHandlerTest extends JDFUtilityTestBase
{

	@Test
	void testConstruct()
	{
		final MyResourceHandler h = new MyResourceHandler("abc", sm_dirTestData);
		assertNotNull(h);
	}

	@Test
	public void testResHandler() throws Exception
	{
		final MyResourceHandler rh = new MyResourceHandler("foo", "dummy");
		assertEquals("http://localhost/nix", rh.update("http://localhost/nix"));
		assertEquals("http://localhost/bar/nix", rh.update("http://localhost/bar/foo/nix"));
		assertEquals("dummy", rh.update(""));
		assertEquals("dummy", rh.update("/"));
		assertEquals("dummy", rh.update("http://localhost"));
	}

	@Test
	public void testHandle() throws Exception
	{
		final MyResourceHandler rh = new MyResourceHandler("foo", "dummy");
		final Request r = mock(Request.class);
		final Context c = mock(Context.class);
		when(r.getContext()).thenReturn(c);
		when(r.getHttpURI()).thenReturn(HttpURI.build(""));
		final Response resp = mock(Response.class);
		assertFalse(rh.handle(r, resp, null));
	}

	@Test
	public void testHandleDown() throws Exception
	{
		final MyResourceHandler rh = new MyResourceHandler("foo", "dummy");
		final Request r = mock(Request.class);
		final Context c = mock(Context.class);
		when(r.getContext()).thenReturn(c);
		when(r.getHttpURI()).thenReturn(HttpURI.build("abcd"));
		when(c.getContextPath()).thenReturn("abc");
		final Response resp = mock(Response.class);
		assertFalse(rh.handle(r, resp, null));
	}

	@Test
	public void testHandleDown2() throws Exception
	{
		final MyResourceHandler rh = new MyResourceHandler("foo", "dummy");
		final Request r = mock(Request.class);
		final Context c = mock(Context.class);
		when(r.getContext()).thenReturn(c);
		when(r.getHttpURI()).thenReturn(HttpURI.build("foo/abcd"));
		when(c.getContextPath()).thenReturn("abc");
		final Response resp = mock(Response.class);
		assertFalse(rh.handle(r, resp, null));
	}

}
