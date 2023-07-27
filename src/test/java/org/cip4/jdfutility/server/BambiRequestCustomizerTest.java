/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

import org.cip4.jdflib.util.PlatformUtil;
import org.cip4.jdfutility.JDFUtilityTestBase;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.server.Request;
import org.junit.Test;
import org.mockito.Mockito;

public class BambiRequestCustomizerTest extends JDFUtilityTestBase
{

	@Test
	public void testConstruct()
	{
		BambiRequestCustomizer rq = new BambiRequestCustomizer();
		BambiRequestCustomizer rq2 = new BambiRequestCustomizer(rq);
		assertEquals(rq.isSniHostCheck(), rq2.isSniHostCheck());
	}

	@Test
	public void testCustomize() throws UnknownHostException
	{
		BambiRequestCustomizer rq = new BambiRequestCustomizer();
		SSLEngine e = Mockito.mock(SSLEngine.class);
		SSLSession s = Mockito.mock(SSLSession.class);
		Mockito.when(e.getSession()).thenReturn(s);

		Request request = Mockito.mock(Request.class);
		rq.customize(e, request);

		Mockito.when(request.getServerName()).thenReturn("localhost");
		rq.customize(e, request);
		Mockito.when(request.getServerName()).thenReturn("127.0.0.1");
		rq.customize(e, request);

		Mockito.when(request.getServerName()).thenReturn(InetAddress.getLocalHost().getHostName());
		rq.customize(e, request);
		Mockito.when(request.getServerName()).thenReturn(InetAddress.getLocalHost().getHostName().toLowerCase());
		rq.customize(e, request);
		if (PlatformUtil.isWindows())
		{
			Mockito.when(request.getServerName()).thenReturn(InetAddress.getLocalHost().getHostName().toUpperCase());
			rq.customize(e, request);
		}
		Mockito.when(request.getServerName()).thenReturn(InetAddress.getLocalHost().getHostAddress());
		rq.customize(e, request);
		Mockito.when(request.getServerName()).thenReturn(InetAddress.getLocalHost().getCanonicalHostName());
		rq.customize(e, request);

	}

	@Test(expected = BadMessageException.class)
	public void testCustomizeBad() throws UnknownHostException
	{
		BambiRequestCustomizer rq = new BambiRequestCustomizer();
		SSLEngine e = Mockito.mock(SSLEngine.class);
		SSLSession s = Mockito.mock(SSLSession.class);
		Mockito.when(e.getSession()).thenReturn(s);

		Request request = Mockito.mock(Request.class);

		Mockito.when(request.getServerName()).thenReturn("notMyHost");
		rq.customize(e, request);

	}

}
