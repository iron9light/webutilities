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
package com.googlecode.webutilities;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.webutilities.common.Constants.*;
import com.googlecode.webutilities.common.Utils;

/**
 * LOTS of things to be implemented here. This is not completed yet.
 *
 * @author rpatil
 * @version 1.0
 */

public class GZIPCompressionFilter implements Filter {

    private FilterConfig filterConfig;

    private String ignoreUserAgentsPattern;

    private long compressionMinSize = DEFAULT_COMPRESSION_SIZE_THRESHOLD; // Anything above ~128KB

    private String ignoreURLPattern;

    private static final String APPLIED_ATTR = CharacterEncodingFilter.class.getName() + ".applied"; //to mark

    private static final Logger logger = Logger.getLogger(GZIPCompressionFilter.class.getName());

    private static enum HEADER {
        USER_AGENT("User-Agent"),
        ACCEPT_ENCODING_HEADER("Accept-Encoding"),
        CONTENT_ENCODING_HEADER("Content-Encoding"),
        CACHE_CONTROL_HEADER("Cache-Control"),
        CONTENT_LENGTH_HEADER("Content-Length"),
        CONTENT_TYPE_HEADER("Content-Type"),
        ETAG_HEADER("ETag"),
        VARY_HEADER("Vary");

        private String key;

        HEADER(String key) {
            this.key = key;
        }

        public String key() {
            return this.key;
        }
    }


    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;

        this.ignoreUserAgentsPattern = filterConfig.getInitParameter("ignoreUserAgentsPattern");
        this.compressionMinSize = Utils.readLong(filterConfig.getInitParameter("compressionMinSize"), this.compressionMinSize);
        this.ignoreURLPattern = filterConfig.getInitParameter("ignoreURLPatten");

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String url = req.getRequestURI();
        String userAgent = req.getHeader(HEADER.USER_AGENT.key());
        if (!(isPathIgnored(url) || isUserAgentIgnored(userAgent))) {//neither URL nor UA is ignored

            request.setAttribute(APPLIED_ATTR, Boolean.TRUE);
            ServletRequest compressedReq = getRequest(request);
            ServletResponse compressedResp = getResponse(request, response);
            chain.doFilter(compressedReq, compressedResp);

        } else {
            chain.doFilter(request, response);
        }
    }

    public boolean isPathIgnored(String path) {
        return path != null && path.matches(ignoreURLPattern);
    }

    public boolean isUserAgentIgnored(String userAgent) {
        return userAgent == null || userAgent.matches(ignoreUserAgentsPattern);
    }

    public void destroy() {
        this.filterConfig = null;
    }

    private ServletRequest getRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentEncoding = httpRequest.getHeader(HEADER.CONTENT_ENCODING_HEADER.key());
        if (contentEncoding == null) {
            logger.finest("Request is not compressed, so not decompressing");
            return request;
        }

        if (!contentEncoding.matches("gzip|deflate")) {//check for valid encoding before decompressing
            logger.severe("Can't decompress request with encoding: " + contentEncoding);
            return request;
        }
        //!TODO return request wrapper
        return request;
    }

    private ServletResponse getResponse(ServletRequest request,
                                        ServletResponse response) {
        if (response.isCommitted() || request.getAttribute(APPLIED_ATTR) != null) {
            logger.severe("Response committed or filter has already been applied.");
            return response;
        }

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            logger.severe("Can't compress non-HTTP request & response");
            return response;
        }

        //!TODO return response wrapper with gzip compression stream
        return response;
    }


}

