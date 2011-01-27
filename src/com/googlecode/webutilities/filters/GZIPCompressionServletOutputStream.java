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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;


/**
 * @author jitendra.takalkar
 */
public class GZIPCompressionServletOutputStream extends ServletOutputStream {
	
	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(GZIPCompressionServletOutputStream.class.getName());
	
	/**
     * The threshold number which decides to compress or not.
     * Users can configure in web.xml to set it to fit their needs.
     */
    protected int compressionThreshold = DEFAULT_COMPRESSION_SIZE_THRESHOLD;

    /**
     * The buffer through which all of our output bytes are passed.
     */
    protected byte[] buffer = null;

    /**
     * The number of data bytes currently in the buffer.
     */
    protected int bufferCount = 0;

    /**
     * The underlying gzip output stream to which we should write data.
     */
    protected GZIPOutputStream gzipStream = null;

    /**
     * Has this stream been closed?
     */
    protected boolean closed = false;

    /**
     * The content length past which we will not write, or -1 if there is
     * no defined content length.
     */
    protected int length = -1;

    /**
     * The response with which this servlet output stream is associated.
     */
    protected HttpServletResponse response = null;

    /**
     * The underlying servlet output stream to which we should write data.
     */
    protected ServletOutputStream output = null;

	 /**
     * Construct a servlet output stream associated with the specified Response.
     *
     * @param response The associated response
     */
    public GZIPCompressionServletOutputStream(HttpServletResponse response) throws IOException{
        super();
        this.closed = false;
        this.response = response;
        this.output = response.getOutputStream();
    }

    /**
     * Set the compressionThreshold number and create buffer for this size
     * 
     * @param threshold Compression Threshold
     */
    protected void setBuffer(int threshold) {
        compressionThreshold = threshold;
        buffer = new byte[compressionThreshold];
        logger.log(Level.INFO,"buffer is set to "+compressionThreshold);
    }
    
    /* (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {        
        logger.log(Level.INFO,"close() @ GZIPCompressionServletOutputStream");     
        if (closed)
            throw new IOException("This output stream has already been closed");

        if (gzipStream != null) {
            flushToGZip();
            gzipStream.close();
            gzipStream = null;
        } else if (bufferCount > 0) {            	
            output.write(buffer, 0, bufferCount);
            bufferCount = 0;
        }
        output.close();
        closed = true;
    }


    /* (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
       	logger.log(Level.INFO,"flush() @ GZIPCompressionServletOutputStream");
        if (closed) 
            throw new IOException("Cannot flush a closed output stream");

        if (gzipStream != null) 
            gzipStream.flush();
    }

    /**
     * Flush GZIP Stream
     * 
     * @throws IOException
     */
    public void flushToGZip() throws IOException {
       	logger.log(Level.INFO,"flushToGZip() @ GZIPCompressionServletOutputStream");
        if (bufferCount > 0) {
        	logger.log(Level.INFO,"flushing out to GZipStream, bufferCount = " + bufferCount);
            writeToGZip(buffer, 0, bufferCount);
            bufferCount = 0;
        }
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
       	logger.log(Level.INFO,"write "+b+" in GZIPCompressionServletOutputStream ");
        if (closed)
            throw new IOException("Cannot write to a closed output stream");

        if (bufferCount >= buffer.length)
        	flushToGZip();

        buffer[bufferCount++] = (byte) b;
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }
    
    /* (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        logger.log(Level.INFO,"write, bufferCount = " + bufferCount + " len = " + len + " off = " + off);
        if (closed)
            throw new IOException("Cannot write to a closed output stream");

        if (len == 0)
            return;

        // Can we write into buffer ?
        if (len <= (buffer.length - bufferCount)) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            return;
        }

        // There is not enough space in buffer. Flush it ...
        flushToGZip();

        // ... and try again. Note, that bufferCount = 0 here !
        if (len <= (buffer.length - bufferCount)) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            return;
        }
        // write direct to gzip
        writeToGZip(b, off, len);
    }

    /**
     * Write to a GZIP Stream
     * 
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    public void writeToGZip(byte b[], int off, int len) throws IOException {
        logger.log(Level.INFO,"writeToGZip, len = " + len);
        if (gzipStream == null) {
           	logger.log(Level.INFO,"new GZIPOutputStream");
            response.addHeader("Content-Encoding", HTTP_ACCEPT_ENCODING_HEADER_GZIP_VALUE);
            gzipStream = new GZIPOutputStream(output);
        }
        gzipStream.write(b, off, len);
    }
    
    /**
     * Has this response stream been closed?
     * @return boolean
     */
    public boolean closed() {
        return (this.closed);
    }

}
