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

package com.googlecode.webutilities.util;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.webutilities.common.Constants.*;

/**
 * Common Utilities provider class
 *
 * @author rpatil
 * @version 1.0
 */
public final class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * @param string       string representation of a int which is to be parsed and read from
     * @param defaultValue in case parsing fails or string is null, returns this default value
     * @return int parsed value or the default value in case parsing failed
     */
    public static int readInt(String string, int defaultValue) {
        int returnValue;
        try {
            returnValue = Integer.parseInt(string);
        } catch (Exception e) {
            returnValue = defaultValue;
        }
        return returnValue;
    }


    /**
     * @param string       string representation of a long which is to be parsed and read from
     * @param defaultValue in case parsing fails or string is null, returns this default value
     * @return long parsed value or the default value in case parsing failed
     */
    public static long readLong(String string, long defaultValue) {
        long returnValue;
        try {
            returnValue = Long.parseLong(string);
        } catch (Exception e) {
            returnValue = defaultValue;
        }
        return returnValue;
    }

    /**
     * @param string       string representation of a boolean (true or false) which is to be parsed and read from
     * @param defaultValue in case string is null or does not contain valid boolean, returns this default value
     * @return boolean parsed value or the default value
     */

    public static boolean readBoolean(String string, boolean defaultValue) {

        if (string == null || !string.toLowerCase().matches("true|false")) {
            return defaultValue;
        } else {
            return "true".equalsIgnoreCase(string);
        }

    }


    /**
     * @param requestURI the URL string
     * @return extension .css or .js etc.
     */
    public static String detectExtension(String requestURI) { //!TODO case sensitivity? http://server/context/path/a.CSS
        String requestURIExtension = null;
        if (requestURI.endsWith(EXT_JS)) {
            requestURIExtension = EXT_JS;
        } else if (requestURI.endsWith(EXT_JSON)) {
            requestURIExtension = EXT_JSON;
        } else if (requestURI.endsWith(EXT_CSS)) {
            requestURIExtension = EXT_CSS;
        }
        return requestURIExtension;
    }

    /**
     * @param filePath - path of the file, whose mime is to be detected
     * @return contentType - mime type of the file
     */
    public static String selectMimeByFile(String filePath) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(filePath);
    }

    /**
     * @param extensionOrFile - .js or .css etc. of full file path in case of image files
     * @return - mime like text/javascript or text/css etc.
     */
    public static String selectMimeForExtension(String extensionOrFile) {
        if (EXT_JS.equals(extensionOrFile)) {
            return MIME_JS;
        } else if (EXT_CSS.equals(extensionOrFile)) {
            return MIME_CSS;
        } else if (EXT_JSON.equals(extensionOrFile)) {
            return MIME_JSON;
        } else {
            return Utils.selectMimeByFile(extensionOrFile);
        }
    }

    //!TODO might have problems, need to test or replace with something better
    public static String buildProperPath(String parentPath, String relativePathFromParent) {
        if (relativePathFromParent == null) return null;

        if (relativePathFromParent.startsWith("./")) {
            relativePathFromParent = relativePathFromParent.replaceFirst("(./)+", "");
        }

        String path;

        if (relativePathFromParent.startsWith("/")) { //absolute
            path = relativePathFromParent;
        } else if (relativePathFromParent.startsWith("../")) {
            while (relativePathFromParent.startsWith("../")) {
                relativePathFromParent = relativePathFromParent.replaceFirst("../", "");
                if (relativePathFromParent.startsWith("./")) {
                    relativePathFromParent = relativePathFromParent.replaceFirst("./", "");
                }
                parentPath = parentPath == null || parentPath.trim().equals("/") ? "/" : new File(parentPath).getParent();
            }
            path = parentPath + File.separator + relativePathFromParent;
        } else {
            path = parentPath + File.separator + relativePathFromParent;
        }

        return path.replaceAll("//", "/").replaceAll("(\\./)+", "");
    }

    /**
     * Calculates simple hash using file size and last modified time.
     *
     * @param resourceRealPath - file path, whose has to be calculated
     * @return - hash string as lastmodified#size
     */
    public static String simpleHashOf(String resourceRealPath) {
        if (resourceRealPath == null) return null;
        File resource = new File(resourceRealPath);
        if (!resource.exists()) return null;
        long lastModified = resource.lastModified();
        long size = resource.length();
        return String.format("%s#%s", lastModified, size);
    }

    /**
     * Split multiple resources with comma eg. if URL is http://server/context/js/a,b,c.js
     * then a.js, b.js and c.js have to be processed and merged together.
     * <p/>
     * b and c can be absolute paths or relative (relative to previous resource) too.
     * <p/>
     * eg.
     * <p/>
     * http://server/context/js/a,/js/libs/b,/js/yui/c.js - absolutes paths for all OR
     * http://server/context/js/a,/js/libs/b,../yui/c.js - relative path used for c.js (relative to b) OR
     * http://server/context/js/a,/js/libs/b,./c.js OR - b & c are in same directory /js/libs
     *
     * @param request HttpServletRequest
     * @return Set of resources to be processed
     */

    public static List<String> findResourcesToMerge(HttpServletRequest request) {

        String contextPath = request.getContextPath();

        String requestURI = request.getRequestURI(); //w/o hostname, starts with context. eg. /context/path/subpath/a,b,/anotherpath/c.js

        String extension = Utils.detectExtension(requestURI);

        if (extension == null) {
            extension = "";
        }

        requestURI = requestURI.replace(contextPath, "").replace(extension, "");//remove the context path & ext. will become /path/subpath/a,b,/anotherpath/c

        String[] resourcesPath = requestURI.split(",");

        List<String> resources = new ArrayList<String>();

        String currentPath = "/"; //default

        for (String filePath : resourcesPath) {

            String path = Utils.buildProperPath(currentPath, filePath) + extension;
            if (filePath == null) continue;

            currentPath = new File(path).getParent();
            if (!resources.contains(path)) {
                resources.add(path);
            }
        }
        return resources;
    }

    /**
     * @param resources      - list of resources paths
     * @param sinceTime      - long value to compare against
     * @param servletContext - servlet context
     * @return true if any of the resources is modified since given time, false otherwise
     */
    public static boolean isAnyResourceModifiedSince(List<String> resources, long sinceTime, ServletContext servletContext) {
        for (String resourcePath : resources) {
            resourcePath = servletContext.getRealPath(resourcePath);
            if (resourcePath == null) continue;
            File resource = new File(resourcePath);
            long lastModified = resource.lastModified();
            if (lastModified > sinceTime) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param resources      - list of resources paths
     * @param servletContext - servlet context
     * @return long - maximum of last modified values of the resources
     */
    public static long getLastModifiedFor(List<String> resources, ServletContext servletContext) {
        long lastModified = 0;
        for (String resourcePath : resources) {
            resourcePath = servletContext.getRealPath(resourcePath);
            if (resourcePath == null) continue;
            File resource = new File(resourcePath);
            long resourceLastModified = resource.lastModified();
            if (resourceLastModified > lastModified) {
                lastModified = resourceLastModified;
            }
        }
        return lastModified;
    }

    /**
     * @param resources      - list of resources
     * @param requestETag    - request ETag from If-None-Match header
     * @param servletContext - servlet context
     * @return true if any resource ETag is modified, false otherwise.
     */
    public static boolean isAnyResourceETagModified(List<String> resources, String requestETag, ServletContext servletContext) {
        String hashForETag = Utils.buildETagForResources(resources, servletContext);
        if (requestETag != null && hashForETag != null) {
            requestETag = requestETag.replace("-gzip", "");//might have been added by gzip filter
            return !requestETag.equals(hashForETag);
        }
        return true;
    }


    /**
     * @param resourcesRelativePath - list of resources
     * @param context               - servlet context
     * @return - String as ETag calculated using simple hash based on size and last modified
     */
    public static String buildETagForResources(List<String> resourcesRelativePath, ServletContext context) {
        String hashForETag = "";
        for (String fullPath : resourcesRelativePath) {
            String hash = Utils.simpleHashOf(context.getRealPath(fullPath));
            hashForETag = hashForETag + (hash != null ? ":" + hash : "");
        }
        return hashForETag.length() > 0 ? hashForETag : null;
    }

    /**
     * @param headerDateString - from request header
     * @return Date object after reading from header string
     */
    public static Date readDateFromHeader(String headerDateString) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_HTTP_HEADER, DEFAULT_LOCALE_US);
        try {
            return simpleDateFormat.parse(headerDateString);
        } catch (Exception e) {
            logger.warning("Date parsing using HTTP header pattern failed.");
        }

        //try another rfc1123
        simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_RFC_1123, DEFAULT_LOCALE_US);
        try {
            return simpleDateFormat.parse(headerDateString);
        } catch (Exception e) {
            logger.warning("Date parsing using RFC_1123 pattern failed.");
        }

        //try another rfc1036
        simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_RFC_1036, DEFAULT_LOCALE_US);
        try {
            return simpleDateFormat.parse(headerDateString);
        } catch (Exception e) {
            logger.warning("Date parsing using RFC_1036 pattern failed.");
        }

        //try another ansi
        simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_ANSI_C, DEFAULT_LOCALE_US);
        try {
            return simpleDateFormat.parse(headerDateString);
        } catch (Exception e) {
            logger.warning("Date is not even ANSI C pattern.");
        }

        return null;
    }

    public static String forHeaderDate(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_HTTP_HEADER, DEFAULT_LOCALE_US);
        return simpleDateFormat.format(time);
    }


    private Utils() {
    } //non instantiable

}
