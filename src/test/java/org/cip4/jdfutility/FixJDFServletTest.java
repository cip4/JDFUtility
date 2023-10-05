package org.cip4.jdfutility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdfutility.CheckJDFServlet.FixCall;
import org.junit.jupiter.api.Test;

public class FixJDFServletTest extends JDFUtilityTestBase
{

	@Test
	public void testXX()
	{
		final XJDFHelper h = new XJDFHelper(EnumVersion.Version_2_0, "J");
		final CheckJDFServlet checkJDFServlet = new CheckJDFServlet();
		final FixCall s = checkJDFServlet.new FixCall(checkJDFServlet, null, null);
		final JDFDoc d1 = s.updateSingle(EnumVersion.Version_2_1, new JDFDoc(h.getRoot().getOwnerDocument()));
		assertNotNull(XJDFHelper.getHelper(d1));
	}

	@Test
	public void testXJ()
	{
		final XJDFHelper h = new XJDFHelper(EnumVersion.Version_2_0, "J");
		final CheckJDFServlet checkJDFServlet = new CheckJDFServlet();
		final FixCall s = checkJDFServlet.new FixCall(checkJDFServlet, null, null);
		final JDFDoc d1 = s.updateSingle(EnumVersion.Version_1_6, new JDFDoc(h.getRoot().getOwnerDocument()));
		assertNull(XJDFHelper.getHelper(d1));
		assertEquals(EnumVersion.Version_1_6, d1.getJDFRoot().getVersion(true));
	}

	@Test
	public void testJJ()
	{
		final JDFDoc d0 = new JDFDoc(ElementName.JDF);
		final CheckJDFServlet checkJDFServlet = new CheckJDFServlet();
		final FixCall s = checkJDFServlet.new FixCall(checkJDFServlet, null, null);
		final JDFDoc d1 = s.updateSingle(EnumVersion.Version_1_6, d0);
		assertNull(XJDFHelper.getHelper(d1));
		assertEquals(EnumVersion.Version_1_6, d1.getJDFRoot().getVersion(true));
	}

	@Test
	public void testJX()
	{
		final JDFDoc d0 = new JDFDoc(ElementName.JDF);
		final CheckJDFServlet checkJDFServlet = new CheckJDFServlet();
		final FixCall s = checkJDFServlet.new FixCall(checkJDFServlet, null, null);
		final JDFDoc d1 = s.updateSingle(EnumVersion.Version_2_1, d0);
		assertNotNull(XJDFHelper.getHelper(d1));
	}

}
