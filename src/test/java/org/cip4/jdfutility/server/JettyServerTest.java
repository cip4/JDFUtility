/*
 * The CIP4 Software License, Version 1.0
 *
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
package org.cip4.jdfutility.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdfutility.JDFUtilityTestBase;
import org.cip4.jdfutility.exe.HTTPDump;
import org.cip4.jdfutility.server.JettyServer.JettySSLData;
import org.junit.jupiter.api.Test;

class JettyServerTest extends JDFUtilityTestBase
{

	static volatile int PORT = 33666;

	private static int getPort()
	{
		return PORT++;
	}

	@Test
	public synchronized void testStart()
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		// ns.setSSLPort(443);
		assertTrue(ns.tryStart());
		ns.stop();
	}

	@Test
	public synchronized void testStartStop()
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		final int t = Thread.activeCount();
		for (int i = 0; i < 10; i++)
		{
			assertTrue(ns.tryStart());
			while (!ns.isStarted())
			{
				ThreadUtil.sleep(42);
			}
			ns.stop();
			while (!ns.isStopped())
			{
				ThreadUtil.sleep(42);
			}
			log.info(i + " " + Thread.activeCount());
			assertEquals(Thread.activeCount(), t, 10);
		}
	}

	@Test
	public synchronized void testSSL1()
	{
		final HTTPDump ns = new HTTPDump();
		ns.setSSLPort(443);
		ns.setSSLPort(getPort());
		ns.setSSLPort(getPort());
	}

	@Test
	public synchronized void testSSLData()
	{
		final HTTPDump ns = new HTTPDump();
		ns.setSSLPort(443);
		final JettySSLData sslData = ns.getSSLData();
		assertNotNull(sslData.toString());
		sslData.setAllowFlakySSL(true);
		assertTrue(sslData.isAllowFlakySSL());
		sslData.setKeystorePath("a");
		assertEquals("a", sslData.getKeystorePath());
		sslData.setKeystoreType("p");
		assertEquals("p", sslData.getKeystoreType());
		sslData.setPassword("p1");
		assertEquals("p1", sslData.getPassword());

	}

	@Test
	public void testSSLDataKeystore()
	{
		final HTTPDump ns = new HTTPDump();
		ns.setSSLPort(443);
		final JettySSLData sslData = ns.getSSLData();
		assertNotNull(sslData.getKeystore());

	}

	@Test
	public synchronized void testUpdateSSL() throws InterruptedException
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		ns.setSSLPort(0);
		ns.setSSLPort(getPort());
		assertTrue(ns.tryStart());
		ns.updateHTTP();
		ns.stop();
		ns.join();
	}

	@Test
	public synchronized void testIsStarted() throws InterruptedException
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		assertFalse(ns.isStarted());
		assertTrue(ns.tryStart());
		for (int i = 0; i < 42; i++)
		{
			if (!ns.isStarted())
				ThreadUtil.sleep(100);
		}

		assertTrue(ns.isStarted());
		ns.stop();
		assertFalse(ns.isStarted());
		ns.join();
	}

	@Test
	public synchronized void testIsRunning() throws InterruptedException
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		ns.start();
		for (int i = 0; i < 42; i++)
		{
			if (!ns.isRunning())
				ThreadUtil.sleep(100);
		}
		assertTrue(ns.isRunning());
		ns.stop();
		assertFalse(ns.isStarted());
		ns.join();
	}

	@Test
	public void testResHandlerWL() throws InterruptedException
	{
		final MyResourceHandler rh = new MyResourceHandler("foo", "dummy");
		rh.addBase("boo");
		assertEquals(null, rh.update("http://host/foo/nix"));
		assertEquals("http://host/boo", rh.update("http://host/foo/boo"));
		assertEquals("dummy", rh.update("boo"));
		assertEquals("http://localhost/boo/nix", rh.update("http://localhost/foo/boo/nix"));
		assertEquals("http://localhost/BOO/nix", rh.update("http://localhost/foo/BOO/nix"));
		assertEquals("http://localhost/BOO/nix", rh.update("http://localhost/FOO/BOO/nix"));
	}

	@Test
	public void testResHandlerString() throws InterruptedException
	{
		final MyResourceHandler rh = new MyResourceHandler("foo", "bar");
		assertNotNull(rh.toString());
	}

}
