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

/**
 * @author jitendra.takalkar
 */
public class GZIPCompressionFilterTest extends BasicServletTestCaseAdapter {
	
	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(GZIPCompressionFilterTest.class.getName());

	/**
	 * Scenario: Size of content less than the Default compression threshold. 
	 * Expected output - content should not compress using gzip algorithm.
	 */
	public void testGZIPCompressionFilterTest() {
		commonSetUpRequest();
		createFilter(com.googlecode.webutilities.filters.GZIPCompressionFilter.class);		
		createServlet(GZIPCompressionFilterTestServlet.class);				
		setDoChain(true);
		doGet();
		logger.log(Level.INFO,getOutput());
		logger.log(Level.INFO,getWebMockObjectFactory().getMockResponse().getHeader("Content-Encoding"));
		assertEquals("gzip|deflate - gzip supported -- able to compress", getOutput());		
	}
	
	/**
	 * Scenario: Compression threshold set to minimum so that it compress the content
	 * Expected output: Response header should contains , content-encoding and content 
	 * should be compressed using gzip algorithm. 
	 */
	public void testCompressionThreshold(){
		commonSetUpRequest();
		getWebMockObjectFactory().getMockFilterConfig().setInitParameter("compressionThreshold", "10");
		createFilter(com.googlecode.webutilities.filters.GZIPCompressionFilter.class);		
		createServlet(GZIPCompressionFilterTestServlet.class);				
		setDoChain(true);
		doGet();
		logger.log(Level.INFO,getWebMockObjectFactory().getMockResponse().getHeader("Content-Encoding"));		
		assertEquals("gzip|deflate",getWebMockObjectFactory().getMockResponse().getHeader("Content-Encoding"));		
	}
	
	/**
	 * Scenario: Ignore User agent pattern .
	 * Expected output: - Content should not be compress using gzip algorith.
	 */
	public void testIgnoreUserAgentsPattern(){
		commonSetUpRequest();
		getWebMockObjectFactory().getMockRequest().addHeader("User-Agent","Mozilla");
		getWebMockObjectFactory().getMockFilterConfig().setInitParameter("ignoreUserAgentsPattern", "Mozilla");		
		createFilter(com.googlecode.webutilities.filters.GZIPCompressionFilter.class);		
		createServlet(GZIPCompressionFilterTestServlet.class);				
		setDoChain(true);
		doGet();
		logger.log(Level.INFO,getOutput());
		assertEquals("gzip not supported", getOutput());
	}
	
	/**
	 * Scenario: Ignore URL Pattern
	 * Expected output: Content should not be compress using gzip algorith.
	 */
	public void testIgnoreURLPatten(){		
		commonSetUpRequest();
		getWebMockObjectFactory().getMockFilterConfig().setInitParameter("ignoreURLPatten", "/gzipcompression/request/uri");	
		createFilter(com.googlecode.webutilities.filters.GZIPCompressionFilter.class);		
		createServlet(GZIPCompressionFilterTestServlet.class);				
		setDoChain(true);
		doGet();
		logger.log(Level.INFO,getOutput());
		assertEquals("gzip not supported", getOutput());	
	}
	
	private void commonSetUpRequest(){
		getWebMockObjectFactory().getMockRequest().setRequestURI("/gzipcompression/request/uri");
		getWebMockObjectFactory().getMockRequest().addHeader("Accept-Encoding","gzip|deflate");			
	}
		

}
