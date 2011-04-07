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
package com.googlecode.webutilities.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.webutilities.util.Utils;

import static com.googlecode.webutilities.common.Constants.*;

/**
 * The <code>JSCSSMergeServet</code> is the Http Servlet to combine multiple JS or CSS static resources in one HTTP request.
 * using YUICompressor.
 * <p>
 * Using <code>JSCSSMergeServet</code> the multiple JS or CSS resources can grouped together (by adding comma) in one HTTP call.
 * </p>
 * <h3>Usage</h3>
 * <p>
 * Put the <b>webutilities-x.y.z.jar</b> in your classpath (WEB-INF/lib folder of your webapp).
 * </p>
 * <p>
 * Declare this servlet in your <code>web.xml</code> ( web descriptor file)
 * </p>
 * <pre>
 * ...
 * &lt;servlet&gt;
 * 	&lt;servlet-name&gt;JSCSSMergeServet&lt;/servlet-name&gt;</b>
 * 	&lt;servlet-class&gt;<b>com.googlecode.webutilities.JSCSSMergeServet</b>&lt;/servlet-class&gt;
 * 	&lt;!-- This init param is optional and default value is minutes for 7 days in future. To expire in the past use negative value. --&gt;
 * 	&lt;init-param&gt;
 * 		&lt;param-name&gt;expiresMinutes&lt;/param-name&gt;
 * 		&lt;param-value&gt;7200&lt;/param-value&gt; &lt;!-- 5 days --&gt;
 * 	&lt;/init-param&gt;
 * 	&lt;!-- This init param is also optional and default value is true. Set it false to override. --&gt;
 * 	&lt;init-param&gt;
 * 		&lt;param-name&gt;useCache&lt;/param-name&gt;
 * 		&lt;param-value&gt;false&lt;/param-value&gt;
 * 	&lt;/init-param&gt;
 *  &lt;/servlet&gt;
 * ...
 * </pre>
 * Map this servlet to serve your JS and CSS resources
 * <pre>
 * ...
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;JSCSSMergeServet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;<b>*.js</b>&lt;/url-pattern&gt;
 *   &lt;url-pattern&gt;<b>*.json</b>&lt;/url-pattern&gt;
 *   &lt;url-pattern&gt;<b>*.css</b>&lt;/url-pattern&gt;
 * &lt;/servlet-mapping>
 * ...
 * </pre>
 * <p>
 * In your web pages (HTML or JSP files) combine your multiple JS or CSS in one request as shown below.
 * </p>
 * <p>To serve multiple JS files through one HTTP request</p>
 * <pre>
 * &lt;script language="JavaScript" src="<b>/myapp/js/prototype,controls,dragdrop,myapp.js</b>"&gt;&lt;/script&gt;
 * </pre>
 * <p>To serve multiple CSS files through one HTTP request</p>
 * <pre>
 * &lt;link rel="StyleSheet" href="<b>/myapp/css/common,calendar,aquaskin.css</b>"/&gt;
 * </pre>
 * <p>
 * Also if you wanted to serve them minified all together then you can add <code>YUIMinFilter</code> on them. See <code>YUIMinFilter</code> from <code>webutilities.jar</code> for details.
 * </p>
 * <h3>Init Parameters</h3>
 * <p>
 * Both init parameters are optional.
 * </p>
 * <p>
 * <b>expiresMinutes</b> has default value of 7 days. This value is relative from current time. Use negative value to expire early in the past.
 * Ideally you should never be using negative value otherwise you won't be able to <b>take advantage of browser caching for static resources</b>.
 * </p>
 * <pre>
 *  <b>expiresMinutes</b> - Relative number of minutes (added to current time) to be set as Expires header
 *  <b>useCache</b> - to cache the earlier merged contents and serve from cache. Default true.
 * </pre>
 * <h3>Dependency</h3>
 * <p>Servlet and JSP api (mostly provided by servlet container eg. Tomcat).</p>
 * <p><b>servlet-api.jar</b> - Must be already present in your webapp classpath</p>
 * <h3>Notes on Cache</h3>
 * <p>If you have not set useCache parameter to false then cache will be used and contents will be always served from cache if found.
 * Sometimes you may not want to use cache or you may want to evict the cache then using URL parameters you can do that.
 * </p>
 * <h4>URL Parameters to skip or evict the cache</h4>
 * <pre>
 * <b>_skipcache_</b> - The JS or CSS request URL if contains this parameters the cache will not be used for it.
 * <b>_dbg_</b> - same as above _skipcache_ parameters.
 * <b>_expirecache_</b> - The cache will be cleaned completely. All existing cached contents will be cleaned.
 * </pre>
 * <pre>
 * <b>Eg.</b>
 * &lt;link rel="StyleSheet" href="/myapp/css/common,calendar,aquaskin.css<b>?_dbg=1</b>"/&gt;
 * or
 * &lt;script language="JavaScript" src="/myapp/js/prototype,controls,dragdrop,myapp.js<b>?_expirecache_=1</b>"&gt;&lt;/script&gt;
 * </pre>
 * <h3>Limitations</h3>
 * <p>
 * The multiple JS or CSS files <b>can be combined together in one request if they are in same parent path</b>. eg. <code><b>/myapp/js/a.js</b></code>, <code><b>/myapp/js/b.js</b></code> and <code><b>/myapp/js/c.js</b></code>
 * can be combined together as <code><b>/myapp/js/a,b,c.js</b></code>. If they are not in common path then they can not be combined in one request. Same applies for CSS too.
 * </p>
 *
 * Visit http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet for more details.
 * Also visit http://code.google.com/p/webutilities/wiki/AddExpiresHeader for details about how to use for setting
 * expires/Cache control header.
 *
 * @author rpatil
 * @version 2.0
 */
public class JSCSSMergeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String INIT_PARAM_EXPIRES_MINUTES = "expiresMinutes";

    public static final String INIT_PARAM_CACHE_CONTROL = "cacheControl";

    public static final String INIT_PARAM_AUTO_CORRECT_URLS_IN_CSS = "autoCorrectUrlsInCSS";

    public static final String INIT_PARAM_TURN_OFF_E_TAG = "turnOffETag";

    private long expiresMinutes = DEFAULT_EXPIRES_MINUTES; //default value 7 days

    private String cacheControl = DEFAULT_CACHE_CONTROL; //default

    private boolean autoCorrectUrlsInCSS = true; //default

    private boolean turnOfETag = false; //default enable eTag

    private static final Logger logger = Logger.getLogger(JSCSSMergeServlet.class.getName());

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.expiresMinutes = Utils.readLong(config.getInitParameter(INIT_PARAM_EXPIRES_MINUTES), this.expiresMinutes);
        this.cacheControl = config.getInitParameter(INIT_PARAM_CACHE_CONTROL) != null ? config.getInitParameter(INIT_PARAM_CACHE_CONTROL) : this.cacheControl ;
        this.autoCorrectUrlsInCSS = Utils.readBoolean(config.getInitParameter(INIT_PARAM_AUTO_CORRECT_URLS_IN_CSS),this.autoCorrectUrlsInCSS);
        this.turnOfETag = Utils.readBoolean(config.getInitParameter(INIT_PARAM_TURN_OFF_E_TAG),this.turnOfETag);
        logger.info("Servlet initialized: " +
                "{" +
                "   " + INIT_PARAM_EXPIRES_MINUTES + ":" + this.expiresMinutes + "" +
                "   " + INIT_PARAM_CACHE_CONTROL + ":" + this.cacheControl + "" +
                "   " + INIT_PARAM_AUTO_CORRECT_URLS_IN_CSS + ":" + this.autoCorrectUrlsInCSS + "" +
                "   " + INIT_PARAM_TURN_OFF_E_TAG + ":" + this.turnOfETag + "" +
                "}");
    }

    /**
     *
     * @param extensionOrFile - .css or .js etc. (lower case) or the absolute path of the file in case of image files
     * @param resourcesToMerge - from request
     * @param hashForETag - from request
     * @param resp - response object
     */
    private void setResponseMimeAndHeaders(String extensionOrFile, List<String> resourcesToMerge, String hashForETag, HttpServletResponse resp) {
        String mime = Utils.selectMimeForExtension(extensionOrFile);
        if(mime != null){
            logger.info("Setting MIME to " + mime);
            resp.setContentType(mime);
        }
        long lastModifiedFor = Utils.getLastModifiedFor(resourcesToMerge, this.getServletContext());
        resp.addDateHeader(HEADER_EXPIRES, lastModifiedFor + expiresMinutes * 60 * 1000);
        resp.addHeader(HTTP_CACHE_CONTROL_HEADER, this.cacheControl);
        resp.addDateHeader(HEADER_LAST_MODIFIED, lastModifiedFor);
        if(hashForETag != null && !this.turnOfETag){
        	resp.addHeader(HTTP_ETAG_HEADER, hashForETag);
        }
        resp.addHeader(HEADER_X_OPTIMIZED_BY, X_OPTIMIZED_BY_VALUE);
        logger.info("Added expires, last-modified & ETag headers");
    }

    /* (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String url = req.getRequestURI();

        logger.info("doGetCalled : " + url);

        Writer out = resp.getWriter();
        ServletContext context = this.getServletContext();
        List<String> resourcesToMerge = Utils.findResourcesToMerge(req);
        String extensionOrFile = Utils.detectExtension(url);
        if(extensionOrFile == null){
            extensionOrFile = resourcesToMerge.get(0);
        }
        //If-None-match
        String requestETag = req.getHeader(HTTP_IF_NONE_MATCH_HEADER);
        if(!this.turnOfETag && !Utils.isAnyResourceETagModified(resourcesToMerge, requestETag, context)){
        	this.sendNotModified(resp);
    		return;
        }
        //If-Modified-Since
        String ifModifiedSince = req.getHeader(HTTP_IF_MODIFIED_SINCE);
        if(ifModifiedSince != null){
            Date date = Utils.readDateFromHeader(ifModifiedSince);
            if(date != null){
                if(!Utils.isAnyResourceModifiedSince(resourcesToMerge, date.getTime(), context)){
                    this.sendNotModified(resp);
                    return;
                }
            }
        }
        String hashForETag = this.turnOfETag ? null : Utils.buildETagForResources(resourcesToMerge, context);
        this.setResponseMimeAndHeaders(extensionOrFile, resourcesToMerge, hashForETag, resp);
        int resourcesNotFound = 0;
        for (String fullPath : resourcesToMerge) {
            logger.info("Processing resource : " + fullPath);
            InputStream is = null;
            try {
                is = this.getServletContext().getResourceAsStream(fullPath);
                if (is != null) {
                    if(fullPath.endsWith(EXT_CSS) && autoCorrectUrlsInCSS){ //Need to deal with images url in CSS
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                        String line;
                        Pattern  pattern = Pattern.compile("[uU][rR][lL]\\s*\\(\\s*['\"]?([^('|\")]*)['\"]?\\s*\\)");
                        while((line = bufferedReader.readLine()) != null){
                            Matcher matcher = pattern.matcher(line);
                            while(matcher.find()){
                                String relativePath = matcher.group(1);
                                if(relativePath.matches("[^(http|ftp|////)].*")){ //ignore paths starting with these
                                    //!TODO can be improved?
                                    line = line.replaceAll(relativePath, req.getContextPath() + Utils.buildProperPath(new File(fullPath).getParent(), relativePath));
                                }
                            }
                            out.write(line+"\n");
                        }
                    }else{
                        int c;
                        while ((c = is.read()) != -1) {
                            out.write(c);
                        }
                    }
                }else{
                    resourcesNotFound++;
                }
            } catch (Exception e) {
                logger.warning("Error while reading resource : " + fullPath);
                logger.severe("Exception :" + e);
            } finally {
                if (is != null) {
                    is.close();
                    out.flush();
                }
            }
        }
        if(resourcesNotFound > 0 && resourcesNotFound == resourcesToMerge.size()){ //all resources not found
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (out != null) {
        	try{
        		resp.setStatus(HttpServletResponse.SC_OK);
                out.close();
        	}catch (Exception e) {
				// ignore
			}
        }
    }

    private void sendNotModified(HttpServletResponse httpServletResponse){
        httpServletResponse.setContentLength(0);
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        logger.info("returning Not Modified (304)");
    }

}
