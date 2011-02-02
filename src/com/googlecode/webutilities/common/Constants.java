package com.googlecode.webutilities.common;


/**
 * Common Class to hold the public static constant so that to share across the project
 *
 * @author rpatil
 * @version 1.0
 */
public final class Constants {


    public static final String TYPE_JS = "js";

    public static final String TYPE_CSS = "css";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String EXT_JS = ".js";

    public static final String EXT_JSON = ".json";

    public static final String EXT_CSS = ".css";

    public static final String MIME_JS = "text/javascript";

    public static final String MIME_JSON = "application/json";

    public static final String MIME_CSS = "text/css";

    public static final String HEADER_EXPIRES = "Expires";

    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    public static final String INIT_PARAM_USE_CACHE = "useCache";

    public static final String PARAM_EXPIRE_CACHE = "_expirecache_";

    public static final String PARAM_SKIP_CACHE = "_skipcache_";

    public static final String PARAM_DEBUG = "_dbg_";

    public static final long DEFAULT_EXPIRES_MINUTES = 7 * 24 * 60; //7 days

    public static final int DEFAULT_COMPRESSION_SIZE_THRESHOLD = 128 * 1024; //128KB
    
    public static final String HTTP_VARY_HEADER = "Vary";
    
    public static final String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    
    public static final String HTTP_CONTENT_ENCODING_HEADER = "Content-Encoding";
    
    public static final String CONTENT_ENCODING_GZIP = "gzip";

    public static final String CONTENT_ENCODING_DEFAULT = "identity";
    
    public static final String CONTENT_ENCODING_DEFLATE = "deflate";
    
    public static final String CONTENT_ENCODING_COMPRESS = "compress";
    
    public static final String HTTP_ACCEPT_ENCODING_HEADER_VALUES_PATTERN = ",?.*\\s*\\b(" + CONTENT_ENCODING_GZIP + ")\\b,?\\s*.*"; //currently only gzip
    
    public static final String HTTP_USER_AGENT_HEADER = "User-Agent";

    private Constants() {
    } //non instantiable


}
