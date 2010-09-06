/**
 * 
 */
package org.cip4.JDFUtility;

import org.cip4.jdflib.core.KElement;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class HTMLUtil
{

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
	 * append an anchor element
	 * @param parent
	 * @param url
	 * @param text if null, simply copy url, else display text
	 */
	public static void appendAnchor(KElement parent, String url, String text)
	{
		if (text == null)
			text = url;

		KElement a = parent.appendElement("a");
		a.setText(text);
		a.setAttribute("href", url);
	}

}
