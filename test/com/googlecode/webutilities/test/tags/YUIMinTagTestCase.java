package com.googlecode.webutilities.test.tags;

import com.googlecode.webutilities.test.common.TestUtils;
import com.googlecode.webutilities.yuimin.YuiMinTag;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.tag.NestedTag;
import com.mockrunner.tag.TagTestModule;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class YUIMinTagTestCase extends TestCase {

    private WebMockObjectFactory webMockObjectFactory = new WebMockObjectFactory();

    private TagTestModule tagTestModule = new TagTestModule(webMockObjectFactory);

    private NestedTag yuiMinTag ;

    private Properties properties = new Properties();

    private int currentTestNumber = 1;

    private static final Logger logger = Logger.getLogger(YUIMinTagTestCase.class.getName());

    public YUIMinTagTestCase() throws Exception {
        properties.load(this.getClass().getResourceAsStream(YuiMinTag.class.getSimpleName() + "Test.properties"));
    }

    private void setUpTag() {
        Map attributeMap = new HashMap();
        String value = properties.getProperty(this.currentTestNumber + ".test.init.params");
        if (value != null && !value.trim().equals("")) {
            String[] params = value.split(",");
            for (String param : params) {
                String[] keyAndValue = param.split(":");
                attributeMap.put(keyAndValue[0],keyAndValue[1]);
            }
        }

        yuiMinTag = tagTestModule.createNestedTag(YuiMinTag.class,attributeMap);

    }

    private void setUpTagBodyContent() throws Exception {
        String resourcesString = properties.getProperty(this.currentTestNumber + ".test.resources");
        if (resourcesString != null && !resourcesString.trim().equals("")) {
            String[] resources = resourcesString.split(",");
            for (String resource : resources) {
                logger.info("Setting resource : " + resource);
                yuiMinTag.addTextChild(TestUtils.readContents(this.getClass().getResourceAsStream(resource)));
            }
        }
    }

    private String getExpectedOutput() throws Exception {

        String expectedResource = properties.getProperty(this.currentTestNumber + ".test.expected");
        if (expectedResource == null || expectedResource.trim().equals("")) return null;
        return TestUtils.readContents(this.getClass().getResourceAsStream(expectedResource));

    }

    private void pre() throws Exception {


        webMockObjectFactory = new WebMockObjectFactory();

        tagTestModule = new TagTestModule(webMockObjectFactory);

        this.setUpTag();

        this.setUpTagBodyContent();

    }

    public void testFilterUsingDifferentScenarios() throws Exception {

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

            yuiMinTag.doLifecycle();

            String actualOutput = tagTestModule.getOutput();

            assertNotNull(actualOutput);

            String expectedOutput = this.getExpectedOutput();

            assertEquals(expectedOutput.trim(), actualOutput.trim());

            this.post();

        }

    }


    private void post() throws Exception {
        this.currentTestNumber++;
    }

}
