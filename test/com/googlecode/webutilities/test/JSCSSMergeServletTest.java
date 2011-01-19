package com.googlecode.webutilities.test;

import com.googlecode.webutilities.JSCSSMergeServlet;
import com.mockrunner.mock.web.*;
import com.mockrunner.servlet.BasicServletTestCaseAdapter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;

public class JSCSSMergeServletTest extends BasicServletTestCaseAdapter {

    private WebMockObjectFactory factory = getWebMockObjectFactory();

    private MockServletConfig servletConfig = factory.getMockServletConfig();

    private MockHttpServletRequest request = factory.getMockRequest();

    private HttpServlet jsCssMergeServlet = new JSCSSMergeServlet();

    private void setUpCSSResources(){
        MockServletContext mockServletContext = factory.getMockServletContext();
        mockServletContext.setResourceAsStream("/css/a.css",this.getClass().getResourceAsStream("resources/css/a.css"));
        mockServletContext.setResourceAsStream("/css/b.css",this.getClass().getResourceAsStream("resources/css/b.css"));
        mockServletContext.setResourceAsStream("/css/c.css",this.getClass().getResourceAsStream("resources/css/c.css"));
    }

    private void setUpJSResources(){
        MockServletContext mockServletContext = factory.getMockServletContext();
        mockServletContext.setResourceAsStream("/js/a.js",this.getClass().getResourceAsStream("resources/js/a.js"));
        mockServletContext.setResourceAsStream("/js/b.js",this.getClass().getResourceAsStream("resources/js/b.js"));
        mockServletContext.setResourceAsStream("/js/c.js",this.getClass().getResourceAsStream("resources/js/c.js"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        servletConfig.setInitParameter("useCache", "false");
        servletConfig.setInitParameter("expiresMinutes", "2"); //one minute
        setServlet(jsCssMergeServlet, true);
        request.setContextPath("/webutilities");
        setUpCSSResources();
        setUpJSResources();
    }

    public void testMergeCSSFiles() throws Exception {

        request.setRequestURI("/css/a,b,c.css");
        jsCssMergeServlet.init();

        doGet();

        String actualOutput = getOutput();
        String expectedOutput = TestUtils.readContents(this.getClass().getResourceAsStream("resources/css/expected-a-b-c.css"));

        assertNotNull(actualOutput);
        assertEquals(expectedOutput.trim(),actualOutput.trim());

    }

    public void testMergeJSFiles() throws Exception {
        request.setRequestURI("/js/a,b,c,a.js");
        jsCssMergeServlet.init();

        doGet();

        String actualOutput = getOutput();
        String expectedOutput = TestUtils.readContents(this.getClass().getResourceAsStream("resources/js/expected-a-b-c.js"));

        assertNotNull(actualOutput);
        assertEquals(expectedOutput.trim(),actualOutput.trim());
    }

}
