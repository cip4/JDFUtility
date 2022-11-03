package org.cip4.jdfutility;

import org.cip4.jdflib.core.JDFCoreConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import javax.servlet.ServletException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class GetFileServletTest {

    @Test
    public void processRequest() throws ServletException, URISyntaxException, IOException {
        Path file = Paths.get(GetFileServlet.class.getResource("/data/resourceInfo.jmf").toURI());

        MockServletConfig config = new MockServletConfig();
        config.addInitParameter("rootDir", file.getParent().toString());

        GetFileServlet servlet = new GetFileServlet();
        servlet.init(config);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo(file.getFileName().toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.processRequest(request, response);

        assertEquals(200, response.getStatus());
        assertEquals(Files.readString(file), response.getContentAsString());
        assertEquals(JDFCoreConstants.MIME_JMF, response.getContentType());
    }

    @Test
    public void processRequestFileNotExists() throws ServletException, URISyntaxException, IOException {
        Path root = Paths.get(GetFileServlet.class.getResource("/data").toURI());

        MockServletConfig config = new MockServletConfig();
        config.addInitParameter("rootDir", root.toString());

        GetFileServlet servlet = new GetFileServlet();
        servlet.init(config);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("file_that_does_not_exist.txt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.processRequest(request, response);

        assertEquals(404, response.getStatus());
        assertEquals("<HTML><H1>Error</H1><br/>Cannot find file: file_that_does_not_exist.txt</HTML>", response.getContentAsString());
        assertEquals("text/html", response.getContentType());
    }

    @Test
    public void processRequestPathTraversal() throws ServletException, IOException {
        MockServletConfig config = new MockServletConfig();
        config.addInitParameter("rootDir", "./");

        GetFileServlet servlet = new GetFileServlet();
        servlet.init(config);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("../attack");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.processRequest(request, response);

        assertEquals(404, response.getStatus());
        assertEquals("<HTML><H1>Error</H1><br/>Cannot find file: ../attack</HTML>", response.getContentAsString());
        assertEquals("text/html", response.getContentType());
    }

    @Test
    public void processRequestInjection() throws ServletException, IOException {
        MockServletConfig config = new MockServletConfig();
        config.addInitParameter("rootDir", "./");

        GetFileServlet servlet = new GetFileServlet();
        servlet.init(config);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("<script>attack</script>");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.processRequest(request, response);

        assertEquals(404, response.getStatus());
        assertEquals("<HTML><H1>Error</H1><br/>Cannot find file: &lt;script&gt;attack&lt;/script&gt;</HTML>", response.getContentAsString());
        assertEquals("text/html", response.getContentType());
    }
}