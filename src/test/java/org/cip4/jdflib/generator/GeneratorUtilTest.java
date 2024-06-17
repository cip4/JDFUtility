package org.cip4.jdflib.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeneratorUtilTest
{

	@Test
	void testVersionAtts()
	{
		assertEquals(GeneratorUtil.getVersionInfoAttributes("required", "1.2", null), "1122222222l");
		assertEquals(GeneratorUtil.getVersionInfoAttributes("required", null, "1.4"), "2222244444l");
	}

}
