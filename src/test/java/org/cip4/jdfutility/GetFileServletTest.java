/*
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;

import org.cip4.jdflib.core.JDFCoreConstants;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

public class GetFileServletTest
{

	@Test
	public void processRequest() throws ServletException, URISyntaxException, IOException
	{
		Path file = Paths.get(GetFileServlet.class.getResource("/data/resourceInfo.jmf").toURI());

		MockServletConfig config = new MockServletConfig();
		config.addInitParameter("rootDir", file.getParent().toString());

		GetFileServlet servlet = new GetFileServlet();
		servlet.init(config);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo(file.getFileName().toString());
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.processRequest(request, response);

		assertEquals(200, response.getStatus());
		assertEquals(Files.readString(file), response.getContentAsString());
		assertEquals(JDFCoreConstants.MIME_JMF, response.getContentType());
	}

	@Test
	public void processRequestFileNotExists() throws ServletException, URISyntaxException, IOException
	{
		Path root = Paths.get(GetFileServlet.class.getResource("/data").toURI());

		MockServletConfig config = new MockServletConfig();
		config.addInitParameter("rootDir", root.toString());

		GetFileServlet servlet = new GetFileServlet();
		servlet.init(config);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("file_that_does_not_exist.txt");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.processRequest(request, response);

		assertEquals(404, response.getStatus());
		assertEquals("<HTML><H1>Error</H1><br/>Cannot find file: file_that_does_not_exist.txt</HTML>", response.getContentAsString());
		assertEquals("text/html", response.getContentType());
	}

	@Test
	public void processRequestPathTraversal() throws ServletException, IOException
	{
		MockServletConfig config = new MockServletConfig();
		config.addInitParameter("rootDir", "./");

		GetFileServlet servlet = new GetFileServlet();
		servlet.init(config);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("../attack");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.processRequest(request, response);

		assertEquals(404, response.getStatus());
		assertEquals("<HTML><H1>Error</H1><br/>Cannot find file: ../attack</HTML>", response.getContentAsString());
		assertEquals("text/html", response.getContentType());
	}

	@Test
	public void processRequestInjection() throws ServletException, IOException
	{
		MockServletConfig config = new MockServletConfig();
		config.addInitParameter("rootDir", "./");

		GetFileServlet servlet = new GetFileServlet();
		servlet.init(config);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setPathInfo("<script>attack</script>");
		MockHttpServletResponse response = new MockHttpServletResponse();

		servlet.processRequest(request, response);

		assertEquals(404, response.getStatus());
		assertEquals("<HTML><H1>Error</H1><br/>Cannot find file: &lt;script&gt;attack&lt;/script&gt;</HTML>", response.getContentAsString());
		assertEquals("text/html", response.getContentType());
	}
}