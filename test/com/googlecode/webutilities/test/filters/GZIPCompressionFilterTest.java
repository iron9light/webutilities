/*
 * Copyright 2010 Rajendra Patil
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.googlecode.webutilities.test.filters;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.webutilities.test.servlets.GZIPCompressionFilterTestServlet;
import com.mockrunner.servlet.BasicServletTestCaseAdapter;

public class GZIPCompressionFilterTest extends BasicServletTestCaseAdapter {
	
	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(GZIPCompressionFilterTest.class.getName());

	/**
	 * Test case for GZIPCompressor Filter
	 */
	public void testGZIPCompressionFilterTest() {
		createFilter(com.googlecode.webutilities.filters.GZIPCompressionFilter.class);		
		createServlet(GZIPCompressionFilterTestServlet.class);
		getWebMockObjectFactory().getMockFilterConfig().setInitParameter("compressionThreshold", "10");
		getWebMockObjectFactory().getMockRequest().setRequestURI("/gzipcompression/request/uri");
		getWebMockObjectFactory().getMockRequest().addHeader("Accept-Encoding","gzip|deflate");
		setDoChain(true);
		doGet();
		logger.log(Level.INFO,getOutput());
		assertEquals("gzip|deflate - gzip supported -- able to compress", getOutput());		
	}
	
	//TODO Few more test case addition is pending

}
