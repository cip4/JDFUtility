/*
 * The CIP4 Software License, Version 1.0
 *
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdfutility.JDFUtilityTestBase;
import org.cip4.jdfutility.exe.HTTPDump;
import org.cip4.jdfutility.server.JettyServer.MyResourceHandler;
import org.junit.Test;

public class JettyServerTest extends JDFUtilityTestBase
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
		assertTrue(ns.tryStart());
		ns.stop();
	}

	@Test
	public synchronized void testStartStop()
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		final int t = Thread.activeCount();
		for (int i = 0; i < 100; i++)
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
		ns.setSSLPort(0, null);
		assertNotNull(ns.setSSLPort(getPort(), null));
		assertNotNull(ns.setSSLPort(getPort(), ns.getDefaultKeyStore()));
	}

	@Test
	public synchronized void testUpdateSSL() throws InterruptedException
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		ns.setSSLPort(0, null);
		assertNotNull(ns.setSSLPort(getPort(), null));
		assertTrue(ns.tryStart());
		ns.updateSSL();
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
		assertTrue(ns.isRunning());
		ns.stop();
		assertFalse(ns.isStarted());
		ns.join();
	}

	@Test
	public void testResHandler() throws InterruptedException
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		MyResourceHandler rh = ns.new MyResourceHandler("foo");
		assertNull(rh.getResource("nix"));
	}

	@Test
	public void testResHandlerString() throws InterruptedException
	{
		final HTTPDump ns = new HTTPDump();
		ns.setPort(getPort());
		MyResourceHandler rh = ns.new MyResourceHandler("foo");
		assertNotNull(rh.toString());
	}

}
