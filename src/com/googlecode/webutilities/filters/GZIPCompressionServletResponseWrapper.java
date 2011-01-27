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
package com.googlecode.webutilities.filters;

import static com.googlecode.webutilities.common.Constants.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author jitendra.takalkar
 */
public class GZIPCompressionServletResponseWrapper extends HttpServletResponseWrapper {
	
	/**
	 * Logger 
	 */
	private Logger logger=Logger.getLogger(GZIPCompressionServletResponseWrapper.class.getName());
	
	/**
	 * Original response 
	 */
	protected HttpServletResponse origResponse = null;

	/**
	 * The ServletOutputStream that has been returned by
	 * <code>getOutputStream()</code>, if any.
	 */
	protected ServletOutputStream stream = null;

	/**
	 * The PrintWriter that has been returned by <code>getWriter()</code>, if
	 * any.
	 */
	protected PrintWriter writer = null;

	/**
	 * The threshold number to compress
	 */
	protected int threshold = DEFAULT_COMPRESSION_SIZE_THRESHOLD;

	/**
	 * Content type
	 */
	protected String contentType = null;


	/**
	 * Calls the parent constructor which creates a ServletResponse adaptor
	 * wrapping the given response object.
	 * 	
	 * @param response HttpServletResponse
	 */
	public GZIPCompressionServletResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
		logger.log(Level.INFO,"GZIPCompressionServletResponseWrapper constructor gets called");
	}


	/**
	 * Set content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
		origResponse.setContentType(contentType);
	}

	/**
	 * Set threshold number
	 */
	public void setCompressionThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * Create and return a ServletOutputStream to write the content associated
	 * with this Response.
	 * 
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	public ServletOutputStream createOutputStream() throws IOException {
		GZIPCompressionServletOutputStream stream = new GZIPCompressionServletOutputStream(
				origResponse);
		stream.setBuffer(threshold);
		return stream;
	}

	/**
	 * Finish a response.
	 */
	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else if (stream != null){
					stream.close();
			}
		} catch (IOException e) {
			logger.log(Level.FINER,e.getMessage(),e);
		}
	}

	/**
	 * Flush the buffer and commit this response.
	 * 
	 * @exception IOException
	 *            if an input/output error occurs
	 */
	public void flushBuffer() throws IOException {
		logger.log(Level.INFO,"flush buffer @ GZIPCompressionServletResponseWrapper");
		((GZIPCompressionServletOutputStream) stream).flush();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null)
			throw new IllegalStateException(
					"getWriter() has already been called for this response");

		if (stream == null)
			stream = createOutputStream();

		return (stream);
	}

	/**
	 * Return the writer associated with this Response.
	 * 
	 * @exception IllegalStateException
	 *                if <code>getOutputStream</code> has already been called
	 *                for this response
	 * @exception IOException
	 *                if an input/output error occurs
	 */
	public PrintWriter getWriter() throws IOException {
		if (writer != null)
			return (writer);

		if (stream != null)
			throw new IllegalStateException(
					"getOutputStream() has already been called for this response");

		stream = createOutputStream();

		// String charset = getCharsetFromContentType(contentType);
		String charEnc = origResponse.getCharacterEncoding();

		// HttpServletResponse.getCharacterEncoding() shouldn't return null
		// according the spec, so feel free to remove that "if"
		if (charEnc != null) {
			writer = new PrintWriter(new OutputStreamWriter(stream, charEnc));
		} else {
			writer = new PrintWriter(stream);
		}

		return (writer);
	}

}
