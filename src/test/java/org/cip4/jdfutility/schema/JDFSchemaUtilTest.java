/**
 * The CIP4 Software License, Version 1.0
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.net.UrlCheck;
import org.cip4.jdfutility.JDFUtilityTestBase;
import org.junit.jupiter.api.Test;

class JDFSchemaUtilTest extends JDFUtilityTestBase
{

	@Test
	void testschema()
	{
		FileUtil.deleteAll(new File(sm_dirTestDataTemp + "tmp/schema"));
		if (new UrlCheck("https://schema.cip4.org").pingRC(123) == 200)
		{
			for (int i = 0; i < 3; i++)
			{
				final File f12 = new File(sm_dirTestDataTemp + "tmp/schema/1.2");
				final File f12a = JDFSchemaUtil.downloadschema(f12, EnumVersion.Version_1_2, 123456);
				assertTrue(f12a.exists());
				final File f19 = new File(sm_dirTestDataTemp + "tmp/schema/1.9/JDF.xsd");
				final File f19a = JDFSchemaUtil.downloadschema(f19, EnumVersion.Version_1_9, 123456);
				assertTrue(f19a.exists());
				final File f20 = new File(sm_dirTestDataTemp + "tmp/schema/2.0/xjdf.xsd");
				final File f20a = JDFSchemaUtil.downloadschema(f20, EnumVersion.Version_2_0, 123456);
				assertTrue(f20a.exists());
				final File f21 = new File(sm_dirTestDataTemp + "tmp/schema/2.1/xjdf.xsd");
				final File f21a = JDFSchemaUtil.downloadschema(f21, EnumVersion.Version_2_1, 123456);
				assertTrue(f21a.exists());
				final File f22 = new File(sm_dirTestDataTemp + "tmp/schema/2.2/xjdf.xsd");
				final File f22a = JDFSchemaUtil.downloadschema(f22, EnumVersion.Version_2_2, 123456);
				assertTrue(f22a.exists());
			}
		}
		else
		{
			final File f12 = new File(sm_dirTestDataTemp + "tmp/schema/1.2");
			final File f12a = JDFSchemaUtil.downloadschema(f12, EnumVersion.Version_1_2, 123456);
			final File f19 = new File(sm_dirTestDataTemp + "tmp/schema/1.9/JDF.xsd");
			final File f19a = JDFSchemaUtil.downloadschema(f19, EnumVersion.Version_1_9, 123456);
			final File f20 = new File(sm_dirTestDataTemp + "tmp/schema/2.0/xjdf.xsd");
			final File f20a = JDFSchemaUtil.downloadschema(f20, EnumVersion.Version_2_0, 123456);
			final File f21 = new File(sm_dirTestDataTemp + "tmp/schema/2.1/xjdf.xsd");
			final File f21a = JDFSchemaUtil.downloadschema(f21, EnumVersion.Version_2_1, 123456);
			final File f22 = new File(sm_dirTestDataTemp + "tmp/schema/2.2/xjdf.xsd");
			final File f22a = JDFSchemaUtil.downloadschema(f22, EnumVersion.Version_2_2, 123456);
		}
	}

	@Test
	void testresSchema()
	{
		assertNotNull(JDFSchemaUtil.getLocalXJDFSchemaDoc());
	}

}
