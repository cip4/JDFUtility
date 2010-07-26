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

}
