/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2011 The International Cooperation for the Integration of 
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * standalone jetty server wrapper
 * 
 * @author rainer prosi
 * @date Dec 9, 2010
 */
public abstract class JettyServer
{
	protected static int thePort = -1;
	protected String context;
	protected Server server;
	protected final Log log;

	/**
	 * @param context 
	 * @param port 
	 *  
	 */
	public JettyServer(String context, int port)
	{
		super();
		log = LogFactory.getLog(getClass());
		setPort(port);
		context = setContext(context);
		log.info("creating JettyServer at context: " + context + " port: " + port);
	}

	protected String setContext(String context)
	{
		if (context == null)
			context = "";
		else if (!context.startsWith("/"))
			context = "/" + context;
		this.context = context;
		return context;
	}

	/**
	 * set the port 
	 * @param port the port to set
	 */
	public static void setPort(int port)
	{
		thePort = port;
	}

	/**
	 *  
	 * 
	 *  
	 */
	public JettyServer()
	{
		super();
		log = LogFactory.getLog(getClass());
		context = "";
		thePort = 8080;
	}

	/**
	 * 
	 * the doing routine to run a jetty server
	 * @throws Exception
	 * @throws InterruptedException
	 */
	public final void runServer() throws Exception, InterruptedException
	{
		server = new Server(thePort);

		HandlerList handlers = new HandlerList();
		server.setHandler(handlers);

		ResourceHandler resourceHandler = createResourceHandler();
		handlers.addHandler(resourceHandler);

		ServletContextHandler context = createServletHandler();
		context.getContextPath();

		handlers.addHandler(context);
		handlers.addHandler(new RedirectHandler());

		server.start();

	}

	/**
	 * get the base url of this server
	 * @return
	 */
	public String getBaseURL()
	{
		try
		{
			return "http://" + InetAddress.getLocalHost().getHostName() + ":" + thePort + context;
		}
		catch (UnknownHostException e)
		{
			return null;
		}
	}

	/**
	 * 
	 * simple resource (file) handler that tweeks the url to match the context, thus allowing servlets to emulate a war file without actually requiring the war file
	 * 
	 * @author rainer prosi
	 * @date Dec 10, 2010
	 */
	private class MyResourceHandler extends ResourceHandler
	{

		protected MyResourceHandler(String strip)
		{
			super();
			this.strip = strip;
		}

		private final String strip;

		@Override
		public Resource getResource(String url) throws MalformedURLException
		{
			if (url.startsWith(strip))
				url = url.substring(strip.length());
			if ("".equals(url) || "/".equals(url))
				return null;
			return super.getResource(url);
		}
	}

	private class RedirectHandler extends AbstractHandler
	{

		/**
		 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
		 */
		public void handle(String pathInContext, Request arg1, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
		{
			response.sendRedirect(getHome());
			arg1.setHandled(true);

		}
	}

	protected ResourceHandler createResourceHandler()
	{
		ResourceHandler resourceHandler = new MyResourceHandler(context);
		resourceHandler.setResourceBase(".");
		return resourceHandler;
	}

	/**
	 * @return the home url, e.g. "."+context+"/overview"
	 */
	protected abstract String getHome();

	protected abstract ServletContextHandler createServletHandler();

	/**
	 * 
	 * @see Server start()
	 */
	public final void start()
	{
		if (server == null)
			try
			{
				runServer();
			}
			catch (Exception e1)
			{
				log.error("Snafu creating server: ", e1);
			}
		try
		{
			server.start();
		}
		catch (Exception e)
		{
			log.error("Snafu starting server: ", e);
		}
	}

	/**
	 * 
	 * @see Server stop()
	 */
	public final void stop()
	{
		if (server != null)
		{
			try
			{
				server.stop();
			}
			catch (Exception e)
			{
				log.error("Snafu stopping server: ", e);
			}
		}
	}

	/**
	 * 
	 * @see Server destroy()
	 */
	public void destroy()
	{
		if (server != null)
			server.destroy();
	}

	/**
	 * get the port number the port is always a singleton in a jetty environment
	 * @return
	 */
	public static int getPort()
	{
		return thePort;
	}

	/**
	 * 
	 * @see #JettyServer.isRunning()
	 * @return
	 */
	public boolean isRunning()
	{
		return server != null && server.isRunning();
	}

	/**
	 * 
	 * @see #JettyServer.isStarted()
	 * @return
	 */
	public boolean isStarted()
	{
		return server != null && server.isStarted();
	}

	/**
	 * 
	 * @see #JettyServer.isStarting()
	 * @return
	 */
	public boolean isStarting()
	{
		return server != null && server.isStarting();
	}

	/**
	 * 
	 * @see #JettyServer.isStopped()
	 * @return
	 */
	public boolean isStopped()
	{
		return server == null || server.isStopped();
	}

	/**
	 * 
	 * @see #JettyServer.isStopping()
	 * @return
	 */
	public boolean isStopping()
	{
		return server != null && server.isStopping();
	}

}
