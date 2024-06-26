package org.cip4.jdflib.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeneratorUtilTest
{

	@Test
	void testVersionAtts()
	{
		assertEquals(GeneratorUtil.getVersionInfoAttributes("required", "1.2", null), "2222222211l");
		assertEquals(GeneratorUtil.getVersionInfoAttributes("required", null, "1.4"), "4444422222l");
	}

}
