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
package com.googlecode.webutilities.test.servlets;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.webutilities.common.Constants.*;

/**
 * @author jitendra.takalkar
 */
@SuppressWarnings("serial")
public class GZIPCompressionFilterTestServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		response.setContentType("text/plain");		

		@SuppressWarnings("unchecked")
		Enumeration<String> e = ((HttpServletRequest) request)
				.getHeaders(HTTP_ACCEPT_ENCODING_HEADER);
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			out.print(name);
			if (name.indexOf(HTTP_ACCEPT_ENCODING_HEADER_GZIP_VALUE) != -1) {
				response.addHeader("Content-Encoding", HTTP_ACCEPT_ENCODING_HEADER_GZIP_VALUE);
				out.print(" - gzip supported -- able to compress");
			} else {
				out.print(" - gzip not supported");
			}
		}
		out.close();
	}

}
