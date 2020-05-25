/**
 *
 *  Copyright (c)   2002 Heidelberger Druckmaschinen AG, All Rights Reserved.
 *  Author:         Kai Mattern
 *  Titel:          SchemaDoc.java
 *  Version:        0.1
 *  Description:
 *
 *  History:        03-13-2002  Kai Mattern started file
 *
 *  TBD:
 */

//package
package org.cip4.jdflib.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.cip4.jdflib.core.DocumentJDFImpl;
import org.cip4.jdflib.core.JDFConstants;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.datatypes.JDFAttributeMap;

//======================================================================================================
//     SchemaDoc
//=======================================================================================================
public class SchemaDoc extends XMLDoc
{
	// class globals

	public SchemaDoc(final DocumentJDFImpl doc)
	{
		super(doc);
	}

	/**
	 * This is the main entrance method after the Generator has parsed the xml file.
	 *
	 * @param String strType - "Message" for Message-Type "Core" for Core-Type "Node" for Node-Type
	 * @param isJava true if the code to be generated is Java. False for C++
	 * 
	 * @return int - zero always
	 */
	public Vector getSchemaInfo(final String strType, final boolean isJava)
	{
		Vector vComplexTypes = new Vector();

		final KElement r = getRoot();
		// Get all complex Types out of the schema
		final VElement vAllComplexTypes = r.getChildrenByTagName("xs:complexType", "", new JDFAttributeMap(), false, true, 0);
		final VElement vAllSimpleTypes = r.getChildrenByTagName("xs:simpleType", "", new JDFAttributeMap(), false, true, 0);
		final VElement vAllElements = r.getChildElementVector("xs:element", "", new JDFAttributeMap(), true, 0, false);

		SchemaComplexType complexType = null;

		// wrap all objects inside a "SchemaComplexType" and add them to main Vector
		final int vAllComplexTypeSize = vAllComplexTypes.size();
		for (int i = 0; i < vAllComplexTypeSize; i++)
		{
			if (((vAllComplexTypeSize - i) % 50) == 0)
				System.out.println(vAllComplexTypeSize - i);

			// Wrap the KElement "Assembly_AssemblySection_AssemblySection_lr"
			// into the SchemaComplexType "Assembly" (leads to duplicates)
			final KElement schemaElement = vAllComplexTypes.elementAt(i);
			System.out.println(schemaElement.getAttribute_KElement("name", "", ""));
			complexType = new SchemaComplexType(schemaElement); // sct.strSchemaComplexTypeName

			complexType = fillComplexType(schemaElement, complexType, vAllComplexTypes, vAllSimpleTypes, vAllElements, vComplexTypes, strType, isJava);

			vComplexTypes.add(complexType);
		}

		vComplexTypes = removeDuplicates(vComplexTypes);

		return vComplexTypes;
	}

	/**
	 * @param schemaElement TODO
	 * @param vAllComplexTypes TODO
	 * @param vAllSimpleTypes
	 * @param vAllElements
	 * @param vComplexTypes
	 * @param strType
	 * @param isJava
	 * @param schemaElement
	 * @return
	 */
	private SchemaComplexType fillComplexType(final KElement schemaElement, final SchemaComplexType complexType, final VElement vAllComplexTypes, final VElement vAllSimpleTypes,
			final VElement vAllElements, final Vector vComplexTypes, final String strType, final boolean isJava)
	{
		// sct.strSchemaComplexTypeName

		// First all names of the complextypes need to be shortened. Some end on '__' '_r' '_re' etc.
		// these "extensions" are not needed here. Cut them away.
		SchemaComplexType complexTypeLocal = GeneratorUtil.unifyComplexTypNames(complexType, strType);

		// set the kind of element "who am i" need for all generation later.
		// set Kind of Code to generate at the moment
		complexTypeLocal.isJava = isJava;
		// typesafe all ComplexTypes for further use
		if ("Message".equals(strType))
		{
			complexTypeLocal.isMessage = true;
		}
		else if ("Core".equals(strType))
		{
			complexTypeLocal.isCore = true;
		}
		else if ("Node".equals(strType))
		{
			complexTypeLocal.isNode = true;
		}
		else if ("CoreMessage".equals(strType))
		{
			complexTypeLocal.isCore = true;
			complexTypeLocal.hasMessage = true;
		}

		final String[] parents = GeneratorUtil.fillParents(complexTypeLocal);
		final VElement vAppInfoElements = new VElement();
		final String motherOf = complexTypeLocal.getStrMotherOfComplexType();

		if (!JDFConstants.EMPTYSTRING.equals(motherOf))
		{
			GeneratorUtil.fillAppInfoElements(motherOf, parents, vAllElements, vAppInfoElements);
			GeneratorUtil.fillAppInfoElements(motherOf, parents, vAllComplexTypes, vAppInfoElements);
		}

		// Take a member out of the schema, process the attributes and put it back into the vector
		complexTypeLocal = GeneratorUtil.getAllValidAttributes(schemaElement, vComplexTypes, vAllSimpleTypes, parents, vAppInfoElements, complexTypeLocal);

		// Take a member out of the schema, process the elements and put it back into the vector
		complexTypeLocal = GeneratorUtil.getAllValidElements(parents, vAppInfoElements, complexTypeLocal);

		// Now its time to get the rest info (enumerations, node String infos etc)
		complexTypeLocal = GeneratorUtil.getRestInfo(parents, vAppInfoElements, complexTypeLocal);

		return complexTypeLocal;
	}

	/**
	 *
	 */
	private Vector removeDuplicates(final Vector vMyCompleteSchema)
	{
		Vector vMyCompleteSchemaLocal = vMyCompleteSchema;

		SchemaComplexType nOldSchemaComplexType;
		SchemaComplexType nNewSchemaComplexType;

		// At this point you have a Vector of all ComplexTyps of the Schema.
		// But quit a few of em are double or triple. Those will now be ripped out.
		final Vector nJustALittleHelper = new Vector();

		while (vMyCompleteSchemaLocal.size() != 0)
		{
			// Ok first dump the first Element in the helper Vector
			nJustALittleHelper.add(vMyCompleteSchemaLocal.elementAt(0));
			// Delete the first element.. its dumped in the Second Vector and no longer needed
			vMyCompleteSchemaLocal.removeElementAt(0);

			// now find the elements with same name...transfer the elements and atttributes unique and delete them
			for (int j = 0; j < vMyCompleteSchemaLocal.size(); j++)
			{
				// ALTER TYP
				nOldSchemaComplexType = (SchemaComplexType) vMyCompleteSchemaLocal.elementAt(j);
				// NEUER TYP
				nNewSchemaComplexType = (SchemaComplexType) nJustALittleHelper.elementAt(nJustALittleHelper.size() - 1);
				// Die Namen der beiden Element. Wichtig zur Feststellung von Gleichheit
				final String str_A_SchemaName = nOldSchemaComplexType.m_SchemaComplexTypeName;
				final String str_B_SchemaName = nNewSchemaComplexType.m_SchemaComplexTypeName;

				// TBD L�se den IF ausdruck auf, das kann ja kein .... lesen
				// im Klartext...wenn der Name des Elements am platz 'i' genauso ist wie der letze hinzugef�gte zum
				// helper vector..
				// dann leg los
				if (str_A_SchemaName != null && str_A_SchemaName.equals(str_B_SchemaName))
				{
					nNewSchemaComplexType = GeneratorUtil.addElementUniqueToVector(nNewSchemaComplexType, nOldSchemaComplexType);
					nNewSchemaComplexType = GeneratorUtil.addAttributeUniqueToVector(nNewSchemaComplexType, nOldSchemaComplexType);

					// something spezial for nodes
					if (nOldSchemaComplexType.isNode)
					{
						// Due to the name unifier there are now 2 some complex Types in the vector
						// one of both has the complete usage string for the elements, because they are definde
						// in his body. The other one has a "empty" usage string. So the one with the empy needs it
						// we dont want to lose it.

						// if you found a empty usage string....
						if (!",".equals(nOldSchemaComplexType.getStrNodeUsageString()))
						{
							// get the other one!
							nNewSchemaComplexType.getAllDataFromComplexType(nOldSchemaComplexType);
						}
					}
					vMyCompleteSchemaLocal.removeElementAt(j);
					// Because i deleted one element it is now needed to decrement j again.
					// The old element x was deleted. The Element x+1 is now Element x
					// If we justwould increment x in the next loop we would let out the old x+1 element
					j -= 1;
				}

				nJustALittleHelper.setElementAt(nNewSchemaComplexType, nJustALittleHelper.size() - 1);
			} // for
		} // while

		// for Nodes exclusive delete out the '_' elements
		// for further information check the architecture of the Node-Schema
		for (int i = 0; i < nJustALittleHelper.size(); i++)
		{
			final SchemaComplexType nSchemaComplexType = (SchemaComplexType) nJustALittleHelper.elementAt(i);
			if (nSchemaComplexType.m_SchemaComplexTypeName.endsWith("_"))
			{
				nJustALittleHelper.removeElementAt(i);
				// cause you remover one member directly from the Vector the n+1 element will be the n element
				// after the remove operation. Therefore, you need to decrement the counter.
				i -= 1;
			}
		}

		vMyCompleteSchemaLocal = nJustALittleHelper;

		// 'init' all members (this writes some elementar informations into the complex-types
		// e.g. names, extends, return values etc..
		for (int i = 0; i < vMyCompleteSchemaLocal.size(); i++)
		{
			final SchemaComplexType nSchemaComplexType = (SchemaComplexType) vMyCompleteSchemaLocal.elementAt(i);
			nSchemaComplexType.setStrSchemaComplexType(); // test
			nSchemaComplexType.m_ExtendOff = GeneratorUtil.getStrExtendsOff(nSchemaComplexType.m_kElem);
		}

		// last but not least, filter out those you dont want
		for (int i = 0; i < vMyCompleteSchemaLocal.size(); i++)
		{
			final String strComplexTypeName = ((SchemaComplexType) vMyCompleteSchemaLocal.elementAt(i)).m_SchemaComplexTypeName;
			if (!GeneratorUtil.isComplexTypeToGenerate(strComplexTypeName))
			{
				vMyCompleteSchemaLocal.removeElementAt(i);
				i -= 1;
			}
		}

		for (int i = 0; i < vMyCompleteSchemaLocal.size(); i++)
		{
			final KElement nPlaceHolder = ((SchemaComplexType) vMyCompleteSchemaLocal.elementAt(i)).m_kElem.getElement("xs:complexContent", "", 0);

			if (nPlaceHolder != null)
			{
				final SchemaComplexType test = new SchemaComplexType(nPlaceHolder);
				final KElement nKElement = test.m_kElem.getElement("xs:extension", "", 0);
				if (nKElement != null)
				{
					String base = nKElement.getAttribute("base", "", "");
					if (base.equals(""))
					{
						base = "jdf:Parameter";
					}

					final SchemaComplexType sct = (SchemaComplexType) vMyCompleteSchemaLocal.elementAt(i);
					sct.setBase(base);
				}
			}
		}

		return vMyCompleteSchemaLocal;
	}

	/**
	 * If you just want to generate a few of the complextypes and not the whole schema, you can call this method The Vector input parameter need to fulfill a view demands. Every Element in this Vector
	 * needs to be from the 'SchemaComplexType' type. The generator needs the Info stored in those typs to generate the specific files.
	 * 
	 * @param Vector nSchemaFragment - the SchemaFragment to generate
	 */
	public static void toCoreJava(final Vector nSchemaFragment, final boolean bGenerateAll)
	{
		String strJavaFile = JDFConstants.EMPTYSTRING;
		SchemaComplexType nSchemaComplexType = null;

		final int schemaFragmentSize = nSchemaFragment.size();
		for (int i = 0; i < schemaFragmentSize; i++)
		{
			if (((schemaFragmentSize - i) % 50) == 0)
				System.out.println(schemaFragmentSize - i);

			nSchemaComplexType = (SchemaComplexType) nSchemaFragment.elementAt(i);

			strJavaFile = JavaCoreStringUtil.getStrJavaCoreFile(nSchemaComplexType);
			writeToFile(Generator.m_strJdfCoreJava, Generator.m_strJdfLostAndFound, nSchemaComplexType.getStrAutoCoreFileName(), strJavaFile, bGenerateAll);
		}
	}

	/**
	 * If you just want to generate a few of the complextypes and not the whole schema, you can call this method The Vector input parameter need to fulfill a view demands. Every Element in this Vector
	 * needs to be from the 'SchemaComplexType' type. The generator needs the Info stored in those typs to generate the specific files.
	 * 
	 * @param Vector nSchemaFragment - the SchemaFragment to generate
	 * @return void - nothing
	 */
	public static void toCoreCpp(final Vector nSchemaFragment, final boolean bGenerateAll)
	{
		String strCppFile = JDFConstants.EMPTYSTRING;
		String strHeaderFile = JDFConstants.EMPTYSTRING;
		SchemaComplexType nSchemaComplexType = null;

		for (int i = 0; i < nSchemaFragment.size(); i++)
		{
			nSchemaComplexType = (SchemaComplexType) nSchemaFragment.elementAt(i);

			strCppFile = CppCoreStringUtil.getStrCppCoreFile(/*nSchemaComplexType*/);
			writeToFile(Generator.m_strJdfCoreCpp, Generator.m_strJdfLostAndFound, nSchemaComplexType.getStrAutoCppCoreFileNameCPP(), strCppFile, bGenerateAll);

			strHeaderFile = CppCoreStringUtil.getStrHeaderFile(nSchemaComplexType);
			writeToFile(Generator.m_strJdfCoreCpp, Generator.m_strJdfLostAndFound, nSchemaComplexType.getStrAutoCppCoreFileNameH(), strHeaderFile, bGenerateAll);
		}
	}

	private static void writeToFile(final String strURLGood, final String strURLBad, final String strFileName, final String strFileContent, final boolean bGenerateAll)
	{
		if (bGenerateAll || isFileToGenerate(strFileName))
		{
			write2file(strURLGood, strFileName, true, strFileContent);
		}
		else
		{
			write2file(strURLBad, strFileName, true, strFileContent);
		}
	}

	/**
	 * after all information was been collected the autofile will be written to hard drive. This method will write a file to the hard drive and overwrite any exisiting file.
	 *
	 * @param
	 * 
	 * @return
	 */
	public static boolean write2file(final String strPath, final String fName, final boolean overwriteFile, final String content)
	{
		String strPathLocal = strPath;

		FileOutputStream fos = null;
		try
		{// global path for library
			final File path = new File(strPathLocal);
			if (!path.exists())
			{
				path.mkdirs();
			}
			if (!path.exists() || !path.isDirectory())
			{
				return false;
			}
			// just the output-file
			if (!strPathLocal.endsWith("/"))
			{
				strPathLocal += "/";
			}
			final File file = new File(strPathLocal + fName);
			if (file.exists())
			{
				// System.err.println(warning +"["+file+"] exists.");
				if (overwriteFile)
				{
					file.delete();
					// System.err.println(warning +"\t and will be overwritten.");
				}
				else
				{
					// System.err.println("");
					return false;
				}
			}

			if (file.createNewFile())
			{
				if (file.canWrite())
				{
					fos = new FileOutputStream(file);
					fos.write(content.getBytes());
				}
			}
		}
		catch (final FileNotFoundException e)
		{
			return false;
		}
		catch (final IOException e)
		{
			return false;
		}
		finally
		{
			if (fos != null)
			{

				try
				{
					fos.close();
				}
				catch (final IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
		return true;
	}

	private static boolean isFileToGenerate(final String strFileName)
	{
		return strFileName == strFileName;
		// return isJavaFile(strFileName) || isCppFile(strFileName) || isHFile(strFileName);
	}

} // class SchemaDoc
