/**
 * 
 */
package org.cip4.JDFUtility;

import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VString;

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
				tr.appendElement("th").setText(col);

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

}
