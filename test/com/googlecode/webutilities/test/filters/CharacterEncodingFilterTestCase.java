package com.googlecode.webutilities.test.filters;

import com.googlecode.webutilities.CharacterEncodingFilter;
import com.googlecode.webutilities.JSCSSMergeServlet;
import com.googlecode.webutilities.common.Constants;
import com.googlecode.webutilities.common.Utils;
import com.googlecode.webutilities.test.common.TestUtils;
import com.googlecode.webutilities.yuimin.YUIMinFilter;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;
import junit.framework.TestCase;

import java.util.Properties;
import java.util.logging.Logger;

public class CharacterEncodingFilterTestCase extends TestCase {

    private JSCSSMergeServlet jscssMergeServlet = new JSCSSMergeServlet();

    private CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();

    private WebMockObjectFactory webMockObjectFactory = new WebMockObjectFactory();

    private ServletTestModule servletTestModule = new ServletTestModule(webMockObjectFactory);

    private Properties properties = new Properties();

    private int currentTestNumber = 1;

    private boolean  force = false;

    private static final Logger logger = Logger.getLogger(CharacterEncodingFilterTestCase.class.getName());

    public static final String TEST_CONTEXT_PATH = "/webutilities";

    public CharacterEncodingFilterTestCase() throws Exception {
        properties.load(this.getClass().getResourceAsStream(CharacterEncodingFilter.class.getSimpleName() + "Test.properties"));
    }

    private void setUpInitParams() {
        String value = properties.getProperty(this.currentTestNumber + ".test.init.params");
        if (value != null && !value.trim().equals("")) {
            String[] params = value.split(",");
            for (String param : params) {
                String[] keyAndValue = param.split(":");
                webMockObjectFactory.getMockFilterConfig().setInitParameter(keyAndValue[0], keyAndValue[1]);
                if("force".equals(keyAndValue[0])){
                    force = Utils.readBoolean(keyAndValue[1],force);
                }
            }
        }

        webMockObjectFactory.getMockServletConfig().setInitParameter(Constants.INIT_PARAM_USE_CACHE,"false"); //never use servlet cache

    }

    private void setUpResources() throws Exception {
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
            if(uriAndQuery.length > 1){
                String[] params = uriAndQuery[1].split("&");
                for(String param: params){
                    String[] nameValue = param.split("=");
                    webMockObjectFactory.getMockRequest().setupAddParameter(nameValue[0],nameValue[1]);
                }

            }
        }
    }

    private String getExpectedEncoding() throws Exception {

        return properties.getProperty(this.currentTestNumber + ".test.expected");

    }

    private void pre() throws Exception {


        webMockObjectFactory = new WebMockObjectFactory();

        servletTestModule = new ServletTestModule(webMockObjectFactory);

        this.setUpInitParams();

        servletTestModule.setServlet(jscssMergeServlet, true);

        servletTestModule.addFilter(characterEncodingFilter,true);

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

            String actualRequestEncoding = webMockObjectFactory.getMockRequest().getCharacterEncoding();

            String actualResponseEncoding = webMockObjectFactory.getMockResponse().getCharacterEncoding();

            String expectedEncoding = this.getExpectedEncoding();

            if(force){
                assertEquals(expectedEncoding.trim(), actualResponseEncoding.trim());
            }

            assertEquals(expectedEncoding, actualRequestEncoding);

            this.post();

        }

    }


    private void post() throws Exception {
        this.currentTestNumber++;
    }

}
