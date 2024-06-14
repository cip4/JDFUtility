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

import java.util.Collection;
import java.util.HashSet;

import org.cip4.jdflib.util.StringUtil;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.EmptyResource;
import org.eclipse.jetty.util.resource.Resource;

/**
 * simple resource (file) handler that tweeks the url to match the context, thus allowing servlets to emulate a war file without actually requiring the war file
 *
 * @author rainer prosi
 * @date Dec 10, 2010
 */
public class MyResourceHandler extends ResourceHandler
{

	private final String home;
	private final Collection<String> whiteList;

	public MyResourceHandler(final String strip, final String home)
	{
		super();
		this.strip = StringUtil.getNonEmpty(strip);
		this.home = home;
		whiteList = new HashSet<>();
	}

	private final String strip;

	@Override
	public Resource getResource(String url)
	{

		if (strip != null && url.startsWith(strip) && (url.length() == strip.length() || url.charAt(strip.length()) == '/'))
		{
			url = url.substring(strip.length());
		}
		try
		{
			if ("".equals(url) || "/".equals(url))
			{
				return super.getResource(home);
			}
			else if (!whiteList.isEmpty())
			{
				final String base = StringUtil.token(url, 0, "/");
				if (!whiteList.contains(base.toLowerCase()))
				{
					return EmptyResource.INSTANCE;
				}
			}

			return super.getResource(url);
		}
		catch (final Exception x)
		{
			return null;
		}
	}

	/**
	 * add a base file to the whitelist. after adding one, all others are blocked
	 * 
	 * @param base
	 */
	public void addBase(final String base)
	{
		whiteList.add(base.toLowerCase());
	}

	@Override
	public String toString()
	{
		return "MyResourceHandler [strip=" + strip + ", getResourceBase()=" + getResourceBase() + "]";
	}
}