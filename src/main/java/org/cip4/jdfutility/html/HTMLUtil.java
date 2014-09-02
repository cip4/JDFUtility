/*
 * The CIP4 Software License, Version 1.0
 *
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
/**
 * 
 */
package org.cip4.jdfutility.html;

import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.core.XMLDoc;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class HTMLUtil
{
	/**
	 * return an empty xhtml root element
	 *  
	 * @return
	 */
	public static KElement createHTMLRoot()
	{
		return new XMLDoc("html", "http://www.w3.org/1999/xhtml").getRoot();
	}

	/**
	 * 
	 * set a css
	 * @param css the url to the css
	 * @param root
	 */
	public static void setCSS(KElement root, final String css)
	{
		root.setXPathAttribute("link/@rel", "stylesheet");
		root.setXPathAttribute("link/@href", css);
		root.setXPathAttribute("link/@type", "text/css");
	}

	/**
	 * 
	 * @param parent 
	 * @param line 
	 */
	public static void appendLine(KElement parent, String line)
	{
		if (line != null)
			parent.appendText("\n" + line);
		parent.appendElement("br");
	}

	/**
	 * 
	 * @param parent 
	 * @param url 
	 */
	public static void appendImage(KElement parent, String url)
	{
		KElement img = parent.appendElement("img");
		img.setAttribute("src", url);
		img.setAttribute("alt", url);
	}

	/**
	 * append an anchor element
	 * @param parent
	 * @param url
	 * @param text if null, simply copy url, else display text
	 * @return the anchor element
	 */
	public static KElement appendAnchor(KElement parent, String url, String text)
	{
		if (text == null)
			text = url;

		KElement a = parent.appendElement("a");
		a.setText(text);
		a.setAttribute("href", url);
		return a;
	}

	/**
	 * append an anchor element
	 * @param parent
	 * @param depth header level (1-6)
	 * @param text  
	 * @return the h1 - h6 element
	 */
	public static KElement appendHeader(KElement parent, int depth, String text)
	{

		if (depth < 1)
			depth = 1;
		if (depth > 6)
			depth = 6;

		KElement h = parent.appendElement("h" + depth);
		h.setText(text);
		return h;
	}

	/**
	 * append a table with a header
	 * @param parent the parent element that receives the table
	 * @param headers the headers to add
	 * @return the table element
	 */
	public static KElement appendTable(KElement parent, VString headers)
	{
		KElement table = parent.appendElement("table");
		if (headers != null)
		{
			KElement tr = table.appendElement("thead").appendElement("tr");
			for (String col : headers)
			{
				tr.appendElement("th").setText(col);
			}
		}
		return table;
	}

	/**
	 * append a row to a table 
	 * @param table the parent table that receives the roe
	 * @param row the row to add
	 * @return the tr (row) element
	 */
	public static KElement appendTableRow(KElement table, VString row)
	{
		KElement tr = table.getCreateElement("tbody").appendElement("tr");
		for (String col : row)
			tr.appendElement("td").setText(col);
		return tr;
	}

	/**
	 * append a row of images to a table 
	 * @param table the parent table that receives the row
	 * @param row the row of urls to add
	 * @return the tr (row) element
	 */
	public static KElement appendTableRowImage(KElement table, VString row)
	{
		KElement tr = table.getCreateElement("tbody").appendElement("tr");
		for (String col : row)
			appendImage(tr.appendElement("td"), col);
		return tr;
	}

	/**
	 * set the title
	 * @param root
	 * @param title
	 */
	public static void setTitle(KElement root, String title)
	{
		root.getCreateXPathElement("head/title").setText(title);
	}

	/**
	 * get a random html color
	 * @return the random color in format #xxxxxx
	 */
	public static String getRandomColor()
	{
		String color = "#";
		for (int i = 0; i < 6; i++)
			color += Integer.toHexString((int) (Math.random() * 16.0));
		return color;
	}

}
