/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cip4.jdflib.util.FileUtil;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Handler.Abstract;
import org.eclipse.jetty.server.Handler.Sequence;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * standalone jetty server wrapper
 *
 * @author rainer prosi
 * @date Dec 9, 2010
 */
public abstract class JettyServer
{
	public static final String BAMBI_SSL_KEYSTORE_PATH = "bambi.ssl.keystore.path";
	public static final String BAMBI_SSL_KEYSTORE_TYPE = "bambi.ssl.keystore.type";
	public static final String BAMBI_SSL_KEYSTORE_PASSWORD = "bambi.ssl.keystore.password";
	public static final String BAMBI_SSL_ALLOWFLAKY = "bambi.ssl.allowflaky";

	public class JettySSLData
	{

		private String keystoreType;
		private String keystorePath;

		public String getKeystoreType()
		{
			return keystoreType;
		}

		public void setKeystoreType(final String keystoretype)
		{
			this.keystoreType = keystoretype;
		}

		public JettySSLData()
		{
			super();
			password = "changeit";
			allowFlakySSL = false;
			keystoreType = KeyStore.getDefaultType();
			keystorePath = getDefaultKeyStorePath();
		}

		String password;
		protected boolean allowFlakySSL;

		public String getPassword()
		{
			return password;
		}

		public void setPassword(final String password)
		{
			this.password = password;
		}

		public boolean isAllowFlakySSL()
		{
			return allowFlakySSL;
		}

		/**
		 * if true, set security levels insanely low - useful for debugging - DO NOT USE IN PRODUCTION
		 * 
		 * @param allowFlakySSL
		 */
		public void setAllowFlakySSL(final boolean allowFlakySSL)
		{
			this.allowFlakySSL = allowFlakySSL;
		}

		/**
		 * @return the selected keystore
		 */
		KeyStore getKeystore()
		{
			KeyStore keyStore;
			try
			{
				keyStore = KeyStore.getInstance(keystoreType);
			}
			catch (final KeyStoreException e)
			{
				log.error("Snafu reading keystore ", e);
				return null;
			}
			final File f = new File(keystorePath);
			if (f.canRead())
			{
				try
				{
					log.info("Reading " + f.getAbsolutePath());
					final BufferedInputStream bufferedInputStream = FileUtil.getBufferedInputStream(f);
					keyStore.load(bufferedInputStream, password.toCharArray());
					bufferedInputStream.close();
					return keyStore;
				}
				catch (final Exception e)
				{
					log.warn("Cannot load keystore at: " + f.getAbsolutePath(), e);
				}
			}
			else
			{
				log.warn("Cannot read " + f.getAbsolutePath());
			}
			return null;
		}

		public String getKeystorePath()
		{
			return keystorePath;
		}

		public void setKeystorePath(final String keystorePath)
		{
			this.keystorePath = keystorePath;
		}

		@Override
		public String toString()
		{
			return "JettySSLData [keystoreType=" + keystoreType + ", keystorePath=" + keystorePath + ", password=" + password + ", allowFlakySSL=" + allowFlakySSL + "]";
		}

	}

	/**
	 * @return
	 */
	public static String getDefaultKeyStorePath()
	{

		final File loc = SystemUtils.getJavaHome();
		if (loc != null)
		{
			final File f = new File(loc, "lib/security/cacerts");
			if (f.canRead())
			{
				return f.getAbsolutePath();
			}
		}
		return null;
	}

	protected int thePort;
	protected int sslPort;
	protected String context;
	protected Server server;
	protected final Log log;
	protected static JettyServer theServer;
	protected final JettySSLData sslData;

	/**
	 * @param context
	 * @param port
	 */
	public JettyServer(String context, final int port)
	{
		super();
		log = LogFactory.getLog(getClass());
		setPort(port);
		context = setContext(context);
		log.info("creating JettyServer at context: " + context + " port: " + port);
		sslPort = -1;
		sslData = new JettySSLData();

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
	 *
	 * @param port the port to set
	 */
	public void setPort(final int port)
	{
		thePort = port;
	}

	/**
	 * @param port the ssl port
	 * @return may be used for additional setup
	 */
	public void setSSLPort(final int port)
	{
		sslPort = port;
	}

	/**
	 * @param port the ssl port
	 * @return may be used for additional setup
	 * @deprecated use the single argument version
	 */
	@Deprecated
	public void setSSLPort(final int port, final String dummy)
	{
		sslPort = port;
	}

	protected String getKeyStorePassword()
	{

		return sslData.getPassword();

	}

	/**
	 *
	 *
	 *
	 */
	public JettyServer()
	{
		this("", 0);
		thePort = getDefaultPort();
	}

	/**
	 * the doing routine to run a jetty server it is generally a bad idea to overwrite this routine - it is not final to allow an empty null server
	 *
	 * @throws Exception
	 * @throws InterruptedException
	 */
	public void runServer() throws Exception
	{
		if (server != null)
		{
			log.error("server already existing - whazzup");
		}
		log.info("creating new server: " + toString());
		server = new Server();
		updateHTTP();
		final Sequence handlers = createHandlerList();
		final Sequence handlerbase = createBaseCollection(handlers);
		server.setHandler(handlerbase);
		server.setDefaultHandler(new RedirectHandler());
		server.start();
		log.info("completed starting new server: " + toString());
	}

	protected void updateHTTP()
	{
		server.setConnectors(null);

		final HttpConfiguration httpConfig = new HttpConfiguration();
		if (sslPort > 0)
		{
			log.info("Updating ssl port to: " + sslPort);

			httpConfig.setSecurePort(sslPort);
			httpConfig.setSecureScheme("https");

			final HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);

			final SecureRequestCustomizer customizer = getRequestCustomizer();
			customizer.setSniHostCheck(!sslData.allowFlakySSL);
			customizer.setSniRequired(!sslData.allowFlakySSL);
			customizer.setStsIncludeSubDomains(sslData.allowFlakySSL);
			httpsConfig.addCustomizer(customizer);

			final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setKeyStore(sslData.getKeystore());
			sslContextFactory.setKeyStorePassword(sslData.getPassword());

			final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
			final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpsConfig);

			final ServerConnector sslConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
			sslConnector.setPort(sslPort);
			server.addConnector(sslConnector);
		}

		if (thePort > 0 && thePort != sslPort)
		{
			log.info("Updating standard port to: " + thePort);
			final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
			final ServerConnector connector = new ServerConnector(server, httpConnectionFactory);
			connector.setPort(thePort);
			server.addConnector(connector);
		}

	}

	/**
	 * hook for more or less secure customizers
	 * 
	 * @return
	 */
	protected BambiRequestCustomizer getRequestCustomizer()
	{
		return new BambiRequestCustomizer();
	}

	/**
	 * @return
	 */
	protected Sequence createHandlerList()
	{
		final Sequence handlers = new Sequence();
		// the resource handler is always first
		final ResourceHandler resourceHandler = createResourceHandler();
		handlers.addHandler(resourceHandler);

		addMoreHandlers(handlers);

		final ServletContextHandler contextHandler = createServletHandler();

		handlers.addHandler(contextHandler);
		handlers.addHandler(new RedirectHandler());
		return handlers;
	}

	/**
	 * create the base handler collection that contains all handlers to be processed multiple times
	 *
	 * @param handlers
	 * @return
	 */
	protected Sequence createBaseCollection(final Sequence handlers)
	{
		final Sequence handlerbase = new Sequence();
		handlerbase.addHandler(handlers);
		addHttpLogger(handlerbase);
		return handlerbase;
	}

	/**
	 * hook to add more handlers if required
	 *
	 * @param handlers
	 */
	protected void addMoreHandlers(final Sequence handlers)
	{
		// nop
	}

	/**
	 * hook to add http loggers or other post handling handlers, if required
	 *
	 * @param handlerbase
	 */
	protected void addHttpLogger(final Sequence handlerbase)
	{
		final RequestLog requestLog = createRequestLog();
		if (requestLog != null)
		{
			server.setRequestLog(requestLog);
			log.info("adding http log handler");
		}
	}

	/**
	 * create a request log - the default method does not
	 *
	 * @return
	 */
	protected RequestLog createRequestLog()
	{
		return null;
	}

	/**
	 * get the base url of this server
	 *
	 * @return
	 */
	public String getBaseURL(final boolean ssl)
	{
		try
		{
			if (ssl)
				return sslPort <= 0 ? null : "https://" + InetAddress.getLocalHost().getHostName() + ":" + sslPort + context;
			else
				return thePort <= 0 ? null : "http://" + InetAddress.getLocalHost().getHostName() + ":" + thePort + context;
		}
		catch (final UnknownHostException e)
		{
			log.fatal("the network looks real bad...", e);
			return null;
		}
	}

	/**
	 * get the base urls of this server
	 *
	 * @return
	 */
	public String getBaseURL()
	{
		final String http = getBaseURL(false);
		final String ssl = getBaseURL(true);
		if (http == null)
			return ssl;
		return http;
	}

	/**
	 * @author rainer prosi
	 */
	private class RedirectHandler extends Abstract
	{

		@Override
		public boolean handle(final Request request, final Response response, final Callback callback) throws Exception
		{
			Response.sendRedirect(request, response, callback, getHome());
			return true;
		}
	}

	/**
	 * @author rainer prosi
	 */
	public class MyResourceHandler extends org.cip4.jdfutility.server.MyResourceHandler
	{

		public MyResourceHandler(final String strip)
		{
			super(strip, getHome());
		}

	}

	/**
	 * @return
	 */
	protected ResourceHandler createResourceHandler()
	{
		final ResourceHandler resourceHandler = new org.cip4.jdfutility.server.MyResourceHandler(context, getHome());
		resourceHandler.setBaseResourceAsString(".");
		resourceHandler.setDirAllowed(false);
		resourceHandler.setWelcomeFiles(List.of("index.html"));
		return resourceHandler;
	}

	/**
	 * @return the home url, e.g. "."+context+"/overview"
	 */
	protected abstract String getHome();

	/**
	 * @return
	 */
	protected abstract int getDefaultPort();

	/**
	 * @return
	 */
	protected abstract ServletContextHandler createServletHandler();

	/**
	 * @see Server start()
	 */
	public void start()
	{
		tryStart();
	}

	/**
	 * @see Server start()
	 */
	public boolean tryStart()
	{
		if (server == null)
		{
			try
			{
				if (sslPort > 0)
				{
					setSSLPort(sslPort);
				}

				runServer();
			}
			catch (final Throwable e1)
			{
				log.error("Snafu creating server at Port: " + thePort + context, e1);
				return false;
			}
		}
		try
		{
			server.start();
		}
		catch (final Throwable e)
		{
			log.error("Snafu starting server: ", e);
			return false;
		}
		log.info("finished starting server " + toString());
		return true;
	}

	/**
	 * @see Server stop()
	 */
	public void stop()
	{
		if (server != null)
		{
			log.info("Stopping server " + toString());
			try
			{
				server.stop();
			}
			catch (final Throwable e)
			{
				log.error("Snafu stopping server: " + toString(), e);
			}
			log.info("Stopped server " + thePort);
		}
		else
		{
			log.warn("Not Stopping null server");
		}
	}

	/**
	 * @see Server destroy()
	 */
	public void destroy()
	{
		if (server != null)
			server.destroy();
	}

	/**
	 * get the port number the port is always a singleton in a jetty environment
	 *
	 * @return
	 */
	public int getPort()
	{
		return thePort;
	}

	/**
	 * @see #JettyServer.isRunning()
	 * @return
	 */
	public boolean isRunning()
	{
		return server != null && server.isRunning();
	}

	/**
	 * @see #JettyServer.isStarted()
	 * @return
	 */
	public boolean isStarted()
	{
		return server != null && server.isStarted();
	}

	/**
	 * @see #JettyServer.isStarting()
	 * @return
	 */
	public boolean isStarting()
	{
		return server != null && server.isStarting();
	}

	/**
	 * @see #JettyServer.isStopped()
	 * @return
	 */
	public boolean isStopped()
	{
		return server == null || server.isStopped();
	}

	/**
	 * @see #JettyServer.isStopping()
	 * @return
	 */
	public boolean isStopping()
	{
		return server != null && server.isStopping();
	}

	/**
	 * @return
	 */
	public static JettyServer getServer()
	{
		return theServer;
	}

	/**
	 *
	 */
	public static void shutdown()
	{
		if (theServer != null)
		{
			theServer.stop();
			try
			{
				theServer.join();
			}
			catch (final InterruptedException e)
			{
				// nop
			}
			theServer = null;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getServerType() + " [context=" + context + ":" + thePort + " ssl=" + sslPort + "]";
	}

	/**
	 * get the server type as a string
	 *
	 * @return
	 */
	public String getServerType()
	{
		return getClass().getSimpleName();
	}

	/**
	 * @param server
	 */
	public static void setServer(final JettyServer server)
	{
		theServer = server;
	}

	/**
	 * @return
	 * @see org.eclipse.jetty.util.component.AbstractLifeCycle#isFailed()
	 */
	public boolean isFailed()
	{
		return server == null || server.isFailed();
	}

	/**
	 * @throws InterruptedException
	 * @see org.eclipse.jetty.server.Server#join()
	 */
	public void join() throws InterruptedException
	{
		if (server != null)
		{
			server.join();
		}
	}

	public int getSSLPort()
	{
		return sslPort;
	}

	public JettySSLData getSSLData()
	{
		return sslData;
	}

}
