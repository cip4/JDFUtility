/**
 *
 * Copyright (c) 2002-2007 Heidelberger Druckmaschinen AG, All Rights Reserved. Author: Kai Mattern Titel: Generator.java Version: 0.1 Description: The xml Schema is partitioned into many "complex
 * type's" these types have children named "attributes" and "elements" this file is for describing all values a "element" can have.
 *
 * History: 03-13-2002 Kai Mattern started file
 *
 * TBD: getMinOccurs should return int
 *
 */

// package
package org.cip4.jdflib.generator;

// imports
import java.util.Vector;

import org.cip4.jdflib.core.XMLDoc;

public class Generator
{
	private static String m_strJdfDevice = "c:/"; // the output device
	private static String m_strJdfSchema = m_strJdfDevice + "gitreps/cip4/JDFSchema/"; // the schema path
	final public static String m_strJdfOutputJava = "c:/schema/JDFLibGeneratorOutput/Java/"; // the output path
	final public static String m_strJdfOutputCpp = "c:/schema/JDFLibGeneratorOutput/Cpp/"; // the output path
	public static String m_strJdfCoreJava = m_strJdfOutputJava + "/src/com/heidelberg/JDFLib/auto"; // the Core files output path for java files
	public static String m_strJdfCoreCpp = m_strJdfOutputCpp + "/src/com/heidelberg/JDFLib/auto"; // the Core files output path for cpp files
	public static String m_strJdfLostAndFound = m_strJdfOutputJava + "/LostAndFound"; // the LostAndFound files output path
	private static String m_strCoreFileName = "JDF.xsd"; // JDF_1_1_C_T_R.xsd JDF_1_1_M_T.xsd

	// main entry point
	public static void main(final String[] argV)
	{
		final XMLDoc doc = XMLDoc.parseFile(m_strJdfSchema + m_strCoreFileName);

		final SchemaDoc javaCoreDoc = new SchemaDoc(doc);

		final Vector vCore = javaCoreDoc.getSchemaInfo("Core", true); // vector for all core Elements from schema
		SchemaDoc.toCoreJava(vCore, false);

		System.exit(0);
	}
}
