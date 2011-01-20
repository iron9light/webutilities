package com.googlecode.webutilities.test;

import com.googlecode.webutilities.test.filters.YUIMinFilterTestCase;
import com.googlecode.webutilities.test.servlets.JSCSSMergeServletTestCase;
import com.googlecode.webutilities.test.tags.YUIMinTagTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test suit for webutilities tests
 */
public class WebutilitiesTestSuit extends TestSuite {

    public static Test suite() {
        TestSuite suite
                = new TestSuite("Test suit for com.googlecode.webutilities");

        suite.addTestSuite(JSCSSMergeServletTestCase.class);
        suite.addTestSuite(YUIMinFilterTestCase.class);
        suite.addTestSuite(YUIMinTagTestCase.class);


        return suite;
    }

}
