package com.googlecode.webutilities.test;

import com.googlecode.webutilities.JSCSSMergeServlet;
import com.mockrunner.mock.web.*;
import com.mockrunner.servlet.ServletTestModule;
import junit.framework.TestCase;

import java.util.Properties;
import java.util.logging.Logger;

public class JSCSSMergeServletTestCase extends TestCase {

    private JSCSSMergeServlet jscssMergeServlet = new JSCSSMergeServlet();

    private WebMockObjectFactory webMockObjectFactory = new WebMockObjectFactory();

    private ServletTestModule servletTestModule = new ServletTestModule(webMockObjectFactory);
    
    private MockServletConfig servletConfig;

    private MockServletContext mockServletContext;

    private MockHttpServletRequest request;

    private Properties properties = new Properties();

    private int currentTestNumber = 1;

    private static final Logger logger = Logger.getLogger(JSCSSMergeServletTestCase.class.getName());

    public static final String TEST_CONTEXT_PATH = "/webutilities";

    public JSCSSMergeServletTestCase() throws Exception {
        properties.load(this.getClass().getResourceAsStream("resources/JSCSSMergeServletTest.properties"));
    }

    private void setUpInitParams() {
        String value = properties.getProperty(this.currentTestNumber + ".test.init.params");
        if (value != null && !value.trim().equals("")) {
            String[] params = value.split(",");
            for (String param : params) {
                String[] keyAndValue = param.split(":");
                servletConfig.setInitParameter(keyAndValue[0], keyAndValue[1]);
            }
        } else { //default
            servletConfig.setInitParameter("useCache", "false");
            servletConfig.setInitParameter("expiresMinutes", "2"); //one minute
        }

    }

    private void setUpResources() throws Exception {
        String resourcesString = properties.getProperty(this.currentTestNumber + ".test.resources");
        if (resourcesString != null && !resourcesString.trim().equals("")) {
            String[] resources = resourcesString.split(",");
            for (String resource : resources) {
                logger.info("Setting resource : " + "/" + resource);
                mockServletContext.setResourceAsStream("/" + resource, this.getClass().getResourceAsStream(resource));
            }
        }
    }

    private void setUpRequest() throws Exception {
        String requestURI = properties.getProperty(this.currentTestNumber + ".test.request.uri");
        request.setContextPath(TEST_CONTEXT_PATH);
        if (requestURI != null && !requestURI.trim().equals("")) {
            request.setRequestURI(requestURI);
        }
        String filter = properties.getProperty(this.currentTestNumber + ".test.filter");
        if (filter != null && !filter.trim().equals("")) {
            Class clazz = Class.forName(filter);
            //MockFilterConfig mockFilterConfig  = webMockObjectFactory.getMockFilterConfig();
            servletTestModule.addFilter(servletTestModule.createFilter(clazz));
            servletTestModule.setDoChain(true);
        }
    }

    private String getExpectedOutput() throws Exception {

        String expectedResource = properties.getProperty(this.currentTestNumber + ".test.expected");
        if (expectedResource == null || expectedResource.trim().equals("")) return null;
        return TestUtils.readContents(this.getClass().getResourceAsStream(expectedResource));

    }

    private void pre() throws java.lang.Exception {

        webMockObjectFactory = new WebMockObjectFactory();

        servletConfig = webMockObjectFactory.getMockServletConfig();

        request = webMockObjectFactory.getMockRequest();

        mockServletContext = webMockObjectFactory.getMockServletContext();

        servletTestModule = new ServletTestModule(webMockObjectFactory);
        this.setUpInitParams();

        servletTestModule.setServlet(jscssMergeServlet, true);

        this.setUpResources();

        this.setUpRequest();
        
        
    }

    public synchronized void testServletUsingDifferentScenarios() throws Exception {

        while (true) {
            this.pre();
            
            String testCase = properties.getProperty(this.currentTestNumber + ".test.name");
            
            if (testCase == null || testCase.trim().equals("")) {
                return; // no more test cases in properties file.
            }
            
            logger.info("Running Test (" + this.currentTestNumber + "): " + testCase + "");

            System.out.println("##################################################################################################################");
            System.out.println("Running Test (" + this.currentTestNumber + "): " + testCase + "");
            System.out.println("##################################################################################################################");
            
            servletTestModule.doGet();

            String actualOutput = servletTestModule.getOutput();

            String expectedOutput = this.getExpectedOutput();

            assertNotNull(actualOutput);

            assertEquals(expectedOutput.trim(), actualOutput.trim());

            this.post();

//            System.out.println("##################################################################################################################");
//            System.out.println("TEST (" + this.currentTestNumber + ") PASSED");
//            System.out.println("##################################################################################################################");

        }

    }


    private void post() throws java.lang.Exception {
        this.currentTestNumber++;
    }

}
