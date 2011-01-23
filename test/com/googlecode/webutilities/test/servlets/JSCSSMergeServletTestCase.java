package com.googlecode.webutilities.test.servlets;

import static com.googlecode.webutilities.common.Constants.*;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.googlecode.webutilities.JSCSSMergeServlet;
import com.googlecode.webutilities.common.Utils;
import com.googlecode.webutilities.test.common.TestUtils;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;

public class JSCSSMergeServletTestCase extends TestCase {

    private JSCSSMergeServlet jscssMergeServlet = new JSCSSMergeServlet();

    private WebMockObjectFactory webMockObjectFactory = new WebMockObjectFactory();

    private ServletTestModule servletTestModule = new ServletTestModule(webMockObjectFactory);

    private Properties properties = new Properties();

    private boolean useCache = false;

    private int expiresMinutes = 2;

    private int currentTestNumber = 1;

    private static final Logger logger = Logger.getLogger(JSCSSMergeServletTestCase.class.getName());

    public static final String TEST_CONTEXT_PATH = "/webutilities";

    public JSCSSMergeServletTestCase() throws Exception {
        properties.load(this.getClass().getResourceAsStream(JSCSSMergeServlet.class.getSimpleName() + "Test.properties"));
    }

    private void setUpInitParams() {
        String value = properties.getProperty(this.currentTestNumber + ".test.init.params");
        if (value != null && !value.trim().equals("")) {
            String[] params = value.split(",");
            for (String param : params) {
                String[] keyAndValue = param.split(":");
                webMockObjectFactory.getMockServletConfig().setInitParameter(keyAndValue[0], keyAndValue[1]);
                if (INIT_PARAM_USE_CACHE.equals(keyAndValue[0])) {
                    useCache = Utils.readBoolean(keyAndValue[1], useCache);
                }
                if (JSCSSMergeServlet.INIT_PARAM_EXPIRES_MINUTES.equals(keyAndValue[0])) {
                    expiresMinutes = Utils.readInt(keyAndValue[1], expiresMinutes);
                }
            }
        } else { //default
            webMockObjectFactory.getMockServletConfig().setInitParameter(INIT_PARAM_USE_CACHE, useCache + "");
            webMockObjectFactory.getMockServletConfig().setInitParameter(JSCSSMergeServlet.INIT_PARAM_EXPIRES_MINUTES, expiresMinutes + ""); //one minute
        }

    }

    private void setUpResources() {
        String resourcesString = properties.getProperty(this.currentTestNumber + ".test.resources");
        if (resourcesString != null && !resourcesString.trim().equals("")) {
            String[] resources = resourcesString.split(",");
            for (String resource : resources) {
                logger.info("Setting resource : " + resource);
                webMockObjectFactory.getMockServletContext().setResourceAsStream(resource, this.getClass().getResourceAsStream(resource));
            }
        }
    }

    private void setUpRequest() throws Exception {
        String requestURI = properties.getProperty(this.currentTestNumber + ".test.request.uri");
        webMockObjectFactory.getMockRequest().setContextPath(TEST_CONTEXT_PATH);
        if (requestURI != null && !requestURI.trim().equals("")) {
            String[] uriAndQuery = requestURI.split("\\?");
            webMockObjectFactory.getMockRequest().setRequestURI(uriAndQuery[0]);
            if (uriAndQuery.length > 1) {
                String[] params = uriAndQuery[1].split("&");
                for (String param : params) {
                    String[] nameValue = param.split("=");
                    webMockObjectFactory.getMockRequest().setupAddParameter(nameValue[0], nameValue[1]);
                }

            }
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

        servletTestModule = new ServletTestModule(webMockObjectFactory);

        this.setUpInitParams();

        servletTestModule.setServlet(jscssMergeServlet, true);

        this.setUpResources();

        this.setUpRequest();


    }

    public boolean hasCorrectDateHeaders() {

        Date lastModified = TestUtils.readDateFromHeader(webMockObjectFactory.getMockResponse().getHeader(HEADER_LAST_MODIFIED));

        Date expires = TestUtils.readDateFromHeader(webMockObjectFactory.getMockResponse().getHeader(HEADER_EXPIRES));

        if (lastModified == null || expires == null) return false;

        long differenceInMinutes = (expires.getTime() - lastModified.getTime()) / 1000 / 60;

        return (differenceInMinutes == expiresMinutes); //ensure difference between last modified and expires is exactly same as we mentioned in test

    }

    public void testServletUsingDifferentScenarios() throws Exception {

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

            assertTrue(this.hasCorrectDateHeaders());

            String actualOutput = servletTestModule.getOutput();

            assertNotNull(actualOutput);

            String expectedOutput = this.getExpectedOutput();

            assertEquals(expectedOutput.trim(), actualOutput.trim());

            this.post();

        }

    }


    private void post() {
        this.currentTestNumber++;
    }

}
