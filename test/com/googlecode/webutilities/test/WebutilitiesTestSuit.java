package com.googlecode.webutilities.test;

import com.googlecode.webutilities.filters.CompressionFilter;
import com.googlecode.webutilities.filters.GZIPCompressionFilter;
import com.googlecode.webutilities.test.filters.*;
import com.googlecode.webutilities.test.servlets.JSCSSMergeServletTest;
import com.googlecode.webutilities.test.tags.YUIMinTagTest;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suit for webutilities tests
 */
public class WebutilitiesTestSuit extends TestSuite {

    public static Test suite() {
        TestSuite suite
                = new TestSuite("Test suit for com.googlecode.webutilities");

        suite.addTestSuite(JSCSSMergeServletTest.class);
        suite.addTestSuite(YUIMinFilterTest.class);
        suite.addTestSuite(YUIMinTagTest.class);
        suite.addTestSuite(CharacterEncodingFilterTest.class);
        suite.addTestSuite(GZIPCompressionFilterTest1.class);
        suite.addTestSuite(CompressionFilterTest.class);


        return suite;
    }

}
