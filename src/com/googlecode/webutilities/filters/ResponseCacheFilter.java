/*
 * Copyright 2010-2011 Rajendra Patil
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
 */

package com.googlecode.webutilities.filters;

import static com.googlecode.webutilities.common.Constants.HTTP_IF_MODIFIED_SINCE;
import static com.googlecode.webutilities.common.Constants.HTTP_IF_NONE_MATCH_HEADER;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.webutilities.common.Constants;
import com.googlecode.webutilities.common.ServletResponseWrapper;
import com.googlecode.webutilities.filters.common.AbstractFilter;
import com.googlecode.webutilities.util.Utils;

/**
 * The <code>ResponseCacheFilter</code> is implemented as Servlet Filter to enable caching of STATIC resources (JS, CSS, static HTML files)
 * <p>
 * This enables the server side caching of the static resources, where client caching is done using JSCSSMergeServlet by setting
 * appropriate expires/Cache-Control headers.
 * </p>
 * <h3>Usage</h3>
 * <p>
 * Put the <b>webutilities-x.y.z.jar</b> in your classpath (WEB-INF/lib folder of your webapp).
 * </p>
 * <p>
 * Declare this filter in your <code>web.xml</code> ( web descriptor file)
 * </p>
 * <pre>
 * ...
 * &lt;filter&gt;
 * 	&lt;filter-name&gt;responseCacheFilter&lt;/filter-name&gt;</b>
 * 	&lt;filter-class&gt;<b>com.googlecode.webutilities.filters.ResponseCacheFilter</b>&lt;/filter-class&gt;
 * &lt;/filter&gt;
 * ...
 * </pre>
 * Map this filter on your JS and CSS resources
 * <pre>
 * ...
 * &lt;filter-mapping&gt;
 *   &lt;filter-name&gt;responseCacheFilter&lt;/filter-name&gt;
 *   &lt;url-pattern&gt;<b>*.js</b>&lt;/url-pattern&gt;
 *   &lt;url-pattern&gt;<b>*.json</b>&lt;/url-pattern&gt;
 *   &lt;url-pattern&gt;<b>*.css</b>&lt;/url-pattern&gt;
 * &lt;/filter-mapping>
 * ...
 * </pre>
 * <p>
 * And you are all done!
 * </p>
 *
 * Visit http://code.google.com/p/webutilities/wiki/ResponseCacheFilter for more details.
 *
 * @author rpatil
 * @version 1.0
 */

public class ResponseCacheFilter extends AbstractFilter {

    private class CacheObject{

        private long time;

        //private long accessCount = 0;

        private ServletResponseWrapper servletResponseWrapper;

        CacheObject(long time, ServletResponseWrapper servletResponseWrapper){
            this.time = time;
            this.servletResponseWrapper = servletResponseWrapper;
        }

        public long getTime() {
            return time;
        }

        public ServletResponseWrapper getServletResponseWrapper() {
            return servletResponseWrapper;
        }

        /*public void increaseAccessCount(){
            accessCount++;
        }

        public long getAccessCount(){
            return this.accessCount;
        }*/

    }

    private Map<String, CacheObject> cache = Collections.synchronizedMap(new LinkedHashMap<String, CacheObject>());

    private int reloadTime = 0;

    private int resetTime = 0;

    private long lastResetTime;

    private static final Logger logger = Logger.getLogger(ResponseCacheFilter.class.getName());

    private static final String INIT_PARAM_RELOAD_TIME = "reloadTime";

    private static final String INIT_PARAM_RESET_TIME = "resetTime";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        this.reloadTime = Utils.readInt(filterConfig.getInitParameter(INIT_PARAM_RELOAD_TIME),reloadTime);

        this.resetTime = Utils.readInt(filterConfig.getInitParameter(INIT_PARAM_RESET_TIME),resetTime);

        lastResetTime = new Date().getTime();

        logger.info("Cache Filter initialized with: " +
                "{" +
                INIT_PARAM_RELOAD_TIME +":"+ reloadTime + "," +
                INIT_PARAM_RESET_TIME + ":" + resetTime + "," +
                "}");

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;

        String url = httpServletRequest.getRequestURI();

        if(!isURLAccepted(url) || !isUserAgentAccepted(httpServletRequest.getHeader(Constants.HTTP_USER_AGENT_HEADER))){
            logger.info("Skipping Cache filter for: " + url);
            logger.info("URL or UserAgent not accepted");
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }


        long now = new Date().getTime();

        CacheObject cacheObject = cache.get(url);

        boolean expireCache = httpServletRequest.getParameter(Constants.PARAM_EXPIRE_CACHE) != null ||
                (cacheObject != null &&  reloadTime > 0 && (now - cacheObject.getTime())/1000 > reloadTime);

        if(expireCache){
            logger.info("Removing Cache for: " + url + " due to URL parameter.");
            cache.remove(url);
        }

        boolean resetCache = httpServletRequest.getParameter(Constants.PARAM_RESET_CACHE) != null ||
                resetTime > 0 && (now - lastResetTime)/1000 > resetTime;

        if(resetCache){
            logger.info("Resetting whole Cache for due to URL parameter.");
            cache.clear();
            lastResetTime = now;
        }

        boolean skipCache = httpServletRequest.getParameter(Constants.PARAM_DEBUG) != null || httpServletRequest.getParameter(Constants.PARAM_SKIP_CACHE) != null;

        if(skipCache){
            filterChain.doFilter(servletRequest, servletResponse);
            logger.info("Skipping Cache for: " + url + " due to URL parameter.");
            return;
        }
        
        List<String> requestedResources = Utils.findResourcesToMerge(httpServletRequest.getContextPath(), url);
        ServletContext context = filterConfig.getServletContext();
        //If-Modified-Since
        String ifModifiedSince = httpServletRequest.getHeader(HTTP_IF_MODIFIED_SINCE);
        if(ifModifiedSince != null){
            Date date = Utils.readDateFromHeader(ifModifiedSince);
            if(date != null){
                if(!Utils.isAnyResourceModifiedSince(requestedResources, date.getTime(), context)){
                    cache.remove(url);
                    this.sendNotModified(httpServletResponse);
                    return;
                }
            }
        }
        //If-None-match
        String requestETag = httpServletRequest.getHeader(HTTP_IF_NONE_MATCH_HEADER);
        if(!Utils.isAnyResourceETagModified(requestedResources, requestETag, null, context)){
            cache.remove(url);
        	this.sendNotModified(httpServletResponse);
    		return;
        }

        boolean cacheFound = false;

        if(cacheObject != null && cacheObject.getServletResponseWrapper() != null){
            if(requestedResources != null && Utils.isAnyResourceModifiedSince(requestedResources, cacheObject.getTime(), context)){
                logger.info("Some resources have been modified since last cache: " + url);
                cache.remove(url);
                cacheFound = false;
            }else{
                logger.info("Found valid cached response.");
                //cacheObject.increaseAccessCount();
                cacheFound = true;
            }
        }

        if(cacheFound){
            logger.info("Returning Cached response.");
            fillResponseFromCache(httpServletResponse, cacheObject.getServletResponseWrapper());
        }else{
            logger.info("Cache not found or invalidated");
            ServletResponseWrapper wrapper = new ServletResponseWrapper(httpServletResponse);
            filterChain.doFilter(servletRequest, wrapper);

            if(isMIMEAccepted(wrapper.getContentType()) && !expireCache && !resetCache && wrapper.getStatus() != HttpServletResponse.SC_NOT_MODIFIED){
            	cache.put(url, new CacheObject(Utils.getLastModifiedFor(requestedResources, context), wrapper));
	            logger.info("Cache added for: " + url);
            }else{
                logger.info("Cache NOT added for: " + url);
                logger.info("is MIME not accepted: " + isMIMEAccepted(wrapper.getContentType()));
                logger.info("is expireCache: " + expireCache);
                logger.info("is resetCache: " + resetCache); 
            }

            httpServletResponse.setCharacterEncoding(wrapper.getCharacterEncoding());
            httpServletResponse.setContentType(wrapper.getContentType());
            httpServletResponse.getOutputStream().write(wrapper.getBytes());
            httpServletResponse.setStatus(wrapper.getStatus());
        }

    }
    
    private void fillResponseFromCache(HttpServletResponse actual, ServletResponseWrapper cache) throws IOException{
    	for(Cookie cookie : cache.getCookies()){
    		actual.addCookie(cookie);
    	}
    	for(String headerName : cache.getHeaders().keySet()){
    		Object value = cache.getHeaders().get(headerName);
    		if(value instanceof Long){
    			actual.addDateHeader(headerName, ((Long) value));
    		}else if(value instanceof Integer){
    			actual.addIntHeader(headerName, ((Integer) value));
    		}else {
    			actual.addHeader(headerName, value.toString());
    		}
    	}
    	actual.setCharacterEncoding(cache.getCharacterEncoding());
    	actual.setContentType(cache.getContentType());
    	actual.getOutputStream().write(cache.getBytes());
    	actual.setStatus(cache.getStatus());
    }

    private void sendNotModified(HttpServletResponse httpServletResponse){
        httpServletResponse.setContentLength(0);
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        logger.info("returning Not Modified (304)");
    }
}





