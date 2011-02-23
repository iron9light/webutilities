package com.googlecode.webutilities.test.filters;

import com.googlecode.webutilities.filters.CompressionFilter;
import com.googlecode.webutilities.filters.GZIPCompressionFilter;
import com.googlecode.webutilities.servlets.JSCSSMergeServlet;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;
import junit.framework.TestCase;

import java.util.Properties;
import java.util.logging.Logger;

import static com.googlecode.webutilities.common.Constants.*;

public class CompressionFilterTest extends TestCase {

    private JSCSSMergeServlet jscssMergeServlet = new JSCSSMergeServlet();

    private CompressionFilter gzipCompressionFilter = new CompressionFilter();

    private WebMockObjectFactory webMockObjectFactory = new WebMockObjectFactory();

    private ServletTestModule servletTestModule = new ServletTestModule(webMockObjectFactory);

    private Properties properties = new Properties();

    private int currentTestNumber = 1;

    private static final Logger logger = Logger.getLogger(CompressionFilterTest.class.getName());

    public static final String TEST_CONTEXT_PATH = "/webutilities";

    public CompressionFilterTest() throws Exception {
        properties.load(this.getClass().getResourceAsStream(CompressionFilter.class.getSimpleName() + "Test.properties"));
    }

    private void setUpInitParams() {
        String value = properties.getProperty(this.currentTestNumber + ".test.init.params");
        if (value != null && !value.trim().equals("")) {
            String[] params = value.split(",");
            for (String param : params) {
                String[] keyAndValue = param.split(":");
                webMockObjectFactory.getMockFilterConfig().setInitParameter(keyAndValue[0], keyAndValue[1]);
            }
        }

        webMockObjectFactory.getMockServletConfig().setInitParameter(INIT_PARAM_USE_CACHE, "false"); //never use servlet cache

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

    private void setUpRequest() {
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
        String userAgent = properties.getProperty(this.currentTestNumber + ".test.request.userAgent");
        if (userAgent != null && !userAgent.trim().equals("")) {
            webMockObjectFactory.getMockRequest().addHeader(HTTP_USER_AGENT_HEADER, userAgent);
        }
        String accept = properties.getProperty(this.currentTestNumber + ".test.request.accept");
        if (accept != null && !accept.trim().equals("")) {
            webMockObjectFactory.getMockRequest().addHeader(HTTP_ACCEPT_ENCODING_HEADER, accept);
        }
    }

    private String getExpectedContentEncoding() {

        return properties.getProperty(this.currentTestNumber + ".test.expected");

    }

    private void pre() throws Exception {


        webMockObjectFactory = new WebMockObjectFactory();

        servletTestModule = new ServletTestModule(webMockObjectFactory);

        this.setUpInitParams();

        servletTestModule.setServlet(jscssMergeServlet, true);

        servletTestModule.addFilter(gzipCompressionFilter, true);

        servletTestModule.setDoChain(true);

        this.setUpResources();

        this.setUpRequest();

    }

    public void testFilterUsingDifferentScenarios() throws Exception {

        while (true) {

            this.pre();

            String testCase = properties.getProperty(this.currentTestNumber + ".test.name");

            if (testCase == null || testCase.trim().equals("")) {
                break;
                //return; // no more test cases in properties file.
            }

            logger.info("Running Test (" + this.currentTestNumber + "): " + testCase + "");

            System.out.println("##################################################################################################################");
            System.out.println("Running Test (" + this.currentTestNumber + "): " + testCase + "");
            System.out.println("##################################################################################################################");

            servletTestModule.doFilter();

            String actualResponseEncoding = webMockObjectFactory.getMockResponse().getHeader(HTTP_CONTENT_ENCODING_HEADER);

            String actualVary = webMockObjectFactory.getMockResponse().getHeader(HTTP_VARY_HEADER);

            String expectedEncoding = this.getExpectedContentEncoding();

            if (expectedEncoding == null || expectedEncoding.trim().equalsIgnoreCase("null")) {
                assertNull("Actual Encoding from response should be null", actualResponseEncoding);
            } else {
                assertNotNull("Actual Encoding expected was " + expectedEncoding + " but found null.", actualResponseEncoding);

                assertEquals(expectedEncoding.trim(), actualResponseEncoding.trim());

                assertEquals(actualVary.trim(), HTTP_ACCEPT_ENCODING_HEADER);
            }

            this.post();

        }

    }

    private void post() {
        this.currentTestNumber++;
    }

}
