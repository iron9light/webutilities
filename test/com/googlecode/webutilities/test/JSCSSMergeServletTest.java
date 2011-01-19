package com.googlecode.webutilities.test;

import com.googlecode.webutilities.JSCSSMergeServlet;
import com.mockrunner.mock.web.*;
import com.mockrunner.servlet.BasicServletTestCaseAdapter;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.util.Properties;

public class JSCSSMergeServletTest extends BasicServletTestCaseAdapter {

    private WebMockObjectFactory factory = getWebMockObjectFactory();

    private MockServletConfig servletConfig = factory.getMockServletConfig();

    private MockHttpServletRequest request = factory.getMockRequest();

    private HttpServlet jsCssMergeServlet = new JSCSSMergeServlet();

    private Properties properties = new Properties();

    private int currentTestNumber = 1;

    public JSCSSMergeServletTest() throws Exception{
        properties.load(this.getClass().getResourceAsStream("resources/JSCSSMergeServletTest.properties"));
    }

    private void setUpInitParams(){
        String value = properties.getProperty(this.currentTestNumber+".test.init.params");
        if(value != null && !value.trim().equals("")){
            String[] params = value.split(",");
            for(String param: params){
                String[] keyAndValue = param.split(":");
                servletConfig.setInitParameter(keyAndValue[0], keyAndValue[1]);
            }
        }else{ //default
            servletConfig.setInitParameter("useCache", "false");
            servletConfig.setInitParameter("expiresMinutes", "2"); //one minute
        }

    }

    private void setUpResources() throws  Exception{
        MockServletContext mockServletContext = factory.getMockServletContext();
        String resourcesString = properties.getProperty(this.currentTestNumber+".test.resources");
        if(resourcesString != null && !resourcesString.trim().equals("")){
            String[] resources = resourcesString.split(",");
            for(String resource: resources){
                System.out.print(resource);
                 mockServletContext.setResourceAsStream("/" + resource , this.getClass().getResourceAsStream(resource));
            }
        }
    }

    private void setUpRequest() throws  Exception{
        MockServletContext mockServletContext = factory.getMockServletContext();
        String requestURI = properties.getProperty(this.currentTestNumber+".test.request.uri");
        request.setContextPath("/webutilities");
        if(requestURI != null && !requestURI.trim().equals("")){
            request.setRequestURI(requestURI);
        }
    }
    private String getExpectedOutput() throws Exception{

        String expectedResource = properties.getProperty(this.currentTestNumber+".test.expected");
        if(expectedResource == null || expectedResource.trim().equals("")) return null;
        return TestUtils.readContents(this.getClass().getResourceAsStream(expectedResource));

    }

    protected void setUp() throws Exception {
        super.setUp();
        this.setUpInitParams();
        this.setServlet(jsCssMergeServlet, true);
        this.setUpResources();
        this.setUpRequest();
        jsCssMergeServlet.init();
    }

    public void testAllTheTestCases() throws Exception {

        while(true){
            String testCase = properties.getProperty(this.currentTestNumber+".test.name");
            if(testCase == null || testCase.trim().equals("")){
                break; // no more test cases in properties file.
            }
            System.out.println("Running test " + testCase);
            doGet();

            String actualOutput = getOutput();
            String expectedOutput = this.getExpectedOutput();

            assertNotNull(actualOutput);
            assertEquals(expectedOutput.trim(),actualOutput.trim());

            this.currentTestNumber++;
        }

    }

    protected void tearDown() throws Exception {

       jsCssMergeServlet.destroy();

    }

}
