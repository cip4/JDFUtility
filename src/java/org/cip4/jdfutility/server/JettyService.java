/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2012 The International Cooperation for the Integration of 
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
package org.cip4.jdfutility.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.util.logging.LogConfigurator;

/**
 * starter / stopper class when using a windows service
 * @see NullService for an example implementation
 * @author rainer prosi
 * @date Oct 26, 2011
 */
public abstract class JettyService
{
	protected final Log log;
	protected JettyServer theServer = null;
	protected static JettyService theService = null;

	/**
	 * this gets the actual server instance
	 * @param args 
	 * @return
	 */
	public abstract JettyServer getServer(String[] args);

	/**
	 * 
	 */
	public JettyService()
	{
		super();
		log = LogFactory.getLog(getClass());
	}

	/**
	 * 
	 * main ... this main obviously won't work but is an example main. see BambiService for a working implementation
	 * @param args
	 * 
	 */
	public static void main(String[] args)
	{
		LogConfigurator.configureLog(".", "jetty.log");
		theService = null;
		theService.doMain(args);
	}

	/**
	 * 
	 * start
	 * @param args
	 */
	public static final void start(String[] args)
	{
		theService.doStart(args);
	}

	/**
	 * 
	 * main ...
	 * @param args
	 */
	public static final void stop(String[] args)
	{
		theService.doStop(args);
	}

	/**
	 * 
	 * 
	 * @param args
	 * @return
	 */
	public int doMain(String[] args)
	{
		log.info("main() called, # args: " + args.length);
		for (String arg : args)
		{
			log.info("arg: " + arg);
		}

		String arg0 = args.length > 0 ? args[0] : "";
		if ("start".equalsIgnoreCase(arg0))
		{
			return doStart(args);
		}
		else if ("stop".equalsIgnoreCase(arg0))
		{
			return doStop(args);
		}
		else
		{
			log.error("Unknown parameter: " + arg0);
			return 1;
		}
	}

	/**
	 * start the actual server
	 * @param args
	 * @return 
	 */
	protected int doStart(String[] args)
	{
		if (theServer != null)
		{
			log.error("server already started - ignoring start");
			return 2;
		}
		theServer = getLicensedServer(args);
		if (theServer == null)
		{
			log.error("server couldn't start");
		}
		else
		{
			theServer.start();
		}
		return 0;
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	private JettyServer getLicensedServer(String[] args)
	{
		if (isLicensed(args))
			return getServer(args);
		else
			return new NullServer();
	}

	/**
	 * you can overwrite this for a license check and return a null server in case licensing fails
	 * this implementation simply checks for the non-existence of an environment variable JettyNoStart
	 * 
	 * @param args
	 * @return
	 */
	protected boolean isLicensed(String[] args)
	{
		boolean isLicensed = System.getenv("JettyNoStart") == null;
		log.info("checking license - isLicensed=" + isLicensed);
		return isLicensed;
	}

	/**
	 * stop the actual server
	 * @param args
	 * @return 
	 */
	protected int doStop(String[] args)
	{
		if (theServer == null)
		{
			log.error("server already stopped - ignoring stop");
			return 2;
		}
		theServer.stop();
		theServer = null;
		return 0;
	}
}
