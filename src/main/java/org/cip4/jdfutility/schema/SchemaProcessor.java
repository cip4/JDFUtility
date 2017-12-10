/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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
package org.cip4.jdfutility.schema;

import java.util.Vector;

import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.elementwalker.BaseElementWalker;
import org.cip4.jdflib.elementwalker.BaseWalker;
import org.cip4.jdflib.elementwalker.BaseWalkerFactory;
import org.cip4.jdflib.extensions.XJDFConstants;
import org.cip4.jdflib.util.VectorMap;

/**
 * class to modify existing schema
 * @author rainer prosi
 *
 */
public class SchemaProcessor extends BaseElementWalker
{
	final VectorMap<String, String> knownBaseAttributes;
	final VectorMap<String, String> knownBaseElements;
	String currentElement;

	/**
	 *
	 */
	public SchemaProcessor()
	{
		super(new BaseWalkerFactory());
		knownBaseAttributes = fillKnownBaseAttributes();
		knownBaseElements = fillKnownBaseElements();
	}

	private VectorMap<String, String> fillKnownBaseAttributes()
	{
		final VectorMap<String, String> m = new VectorMap<>();
		m.putOne(ElementName.DEVICE, AttributeName.DEVICEID);
		m.putOne(ElementName.GANGINFO, AttributeName.GANGNAME);
		m.putOne(ElementName.GENERALID, AttributeName.IDUSAGE);
		m.putOne(ElementName.GENERALID, AttributeName.IDVALUE);
		m.putOne(XJDFConstants.Header, AttributeName.DEVICEID);
		m.putOne(XJDFConstants.Header, AttributeName.TIME);
		m.putOne(XJDFConstants.Intent, AttributeName.NAME);
		m.putOne(ElementName.JOBPHASE, AttributeName.JOBID);
		m.putOne(ElementName.PIPEPARAMS, AttributeName.OPERATION);
		m.putOne(ElementName.PIPEPARAMS, AttributeName.PIPEID);
		m.putOne(ElementName.QUEUESUBMISSIONPARAMS, AttributeName.URL);
		m.putOne(XJDFConstants.ResourceSet, AttributeName.NAME);
		m.putOne(ElementName.RESUBMISSIONPARAMS, AttributeName.QUEUEENTRYID);
		m.putOne(ElementName.RESUBMISSIONPARAMS, AttributeName.UPDATEMETHOD);
		m.putOne(ElementName.RESUBMISSIONPARAMS, AttributeName.URL);
		m.putOne(ElementName.RETURNQUEUEENTRYPARAMS, AttributeName.QUEUEENTRYID);
		m.putOne(ElementName.RETURNQUEUEENTRYPARAMS, AttributeName.URL);
		m.putOne(ElementName.SUBSCRIPTION, AttributeName.URL);
		m.putOne(XJDFConstants.XJDF, AttributeName.JOBID);
		return m;
	}

	private VectorMap<String, String> fillKnownBaseElements()
	{
		final VectorMap<String, String> m = new VectorMap<>();
		m.putOne("Audit", XJDFConstants.Header);
		m.putOne(XJDFConstants.XJMF, XJDFConstants.Header);
		m.putOne(XJDFConstants.ProductList, JDFConstants.PRODUCT);
		m.putOne(XJDFConstants.XJMF, XJDFConstants.Header);
		m.putOne("Message", XJDFConstants.Header);
		return m;
	}

	public void makeChangeOrder(final KElement e)
	{
		walkTree(e, null);
	}

	/**
	 * the link and ref walker
	 *
	 * @author prosirai
	 *
	 */
	public class WalkDefault extends BaseWalker
	{
		/**
		 *
		 */
		public WalkDefault()
		{
			super(getFactory());
		}
	}

	public class WalkAttribute extends BaseWalker
	{
		/**
		 *
		 */
		public WalkAttribute()
		{
			super(getFactory());
		}

		/**
		 * @see org.cip4.jdflib.elementwalker.BaseWalker#getElementNames()
		 */
		@Override
		public VString getElementNames()
		{
			return new VString("attribute", null);
		}

		/**
		 * @see org.cip4.jdflib.elementwalker.BaseWalker#walk(org.cip4.jdflib.core.KElement, org.cip4.jdflib.core.KElement)
		 */
		@Override
		public KElement walk(final KElement e, final KElement trackElem)
		{
			updateUse(e);
			return super.walk(e, trackElem);
		}

		void updateUse(final KElement e)
		{
			if (!isBaseAttribute(e))
			{
				e.setAttribute("use", "optional");
			}
		}

		boolean isBaseAttribute(final KElement e)
		{
			final Vector<String> atts = knownBaseAttributes.get(currentElement);
			return atts != null && atts.contains(e.getAttribute("name"));
		}
	}

	public class WalkElement extends BaseWalker
	{
		/**
		 *
		 */
		public WalkElement()
		{
			super(getFactory());
		}

		/**
		 * @see org.cip4.jdflib.elementwalker.BaseWalker#getElementNames()
		 */
		@Override
		public VString getElementNames()
		{
			return new VString("element", null);
		}

		/**
		 * @see org.cip4.jdflib.elementwalker.BaseWalker#walk(org.cip4.jdflib.core.KElement, org.cip4.jdflib.core.KElement)
		 */
		@Override
		public KElement walk(final KElement e, final KElement trackElem)
		{
			final String name = e.getNonEmpty("name");
			if (name != null)
			{
				currentElement = name;
			}
			updateMinOccurs(e);
			return super.walk(e, trackElem);
		}

		/**
		 *
		 * @param e
		 */
		void updateMinOccurs(final KElement e)
		{
			if (!isBaseElement(e) && e.hasAttribute("minOccurs"))
			{
				e.setAttribute("minOccurs", "0");
			}
		}

		/**
		 *
		 * @param e
		 * @return
		 */
		boolean isBaseElement(final KElement e)
		{
			final Vector<String> elems = knownBaseElements.get(currentElement);
			return elems != null && elems.contains(e.getAttribute("ref"));
		}
	}

}
