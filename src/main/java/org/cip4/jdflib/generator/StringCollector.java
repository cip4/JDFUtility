/**
 * 
 */
package org.cip4.jdflib.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.cip4.jdflib.util.ContainerUtil;

/**
 * @author rainer prosi
 *
 */
public class StringCollector
{
	private static StringCollector attribs = new StringCollector();
	private static StringCollector elems = new StringCollector();
	public final HashMap<String, String> stringmap;

	public static StringCollector getAttribs()
	{
		return attribs;
	};

	public static StringCollector getElems()
	{
		return elems;
	};

	private StringCollector()
	{
		stringmap = new HashMap<String, String>();
	}

	public void add(String s)
	{
		stringmap.put(s.toUpperCase(), s);
	}

	public void flush(String className)
	{
		Vector<String> v = ContainerUtil.getKeyVector(stringmap);
		Collections.sort(v);
		File f = new File(className + ".txt");
		try
		{
			PrintStream s = new PrintStream(f);
			for (String key : v)
			{
				s.println("/** */");
				s.println("public static final String " + key + " = \"" + stringmap.get(key) + "\";");
				s.println();
			}
			s.flush();
			s.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
