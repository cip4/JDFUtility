package org.cip4.jdflib.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GeneratorUtilTest
{

	@Test
	void testVersionAtts()
	{
		assertEquals("1122222222l", GeneratorUtil.getVersionInfoAttributes("required", "1.2", null));
		assertEquals("2222244444l", GeneratorUtil.getVersionInfoAttributes("required", null, "1.4"));
	}

}
