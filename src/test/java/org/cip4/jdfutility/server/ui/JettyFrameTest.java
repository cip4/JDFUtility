package org.cip4.jdfutility.server.ui;

import org.cip4.jdfutility.exe.HTTPDump;
import org.cip4.jdfutility.exe.HTTPFrame;
import org.junit.Test;

public class JettyFrameTest
{

	@Test
	public void testStart()
	{
		HTTPDump dump = new HTTPDump();
		dump.setSSLPort(123);

		final HTTPFrame frame = new HTTPFrame(dump);
		frame.started();
		frame.stopped();
	}

}
