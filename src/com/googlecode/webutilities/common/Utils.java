package com.googlecode.webutilities.common;

import static com.googlecode.webutilities.common.Constants.*;
/**
 * Common Utilities provider class
 *
 * @author rpatil
 * @version 1.0
 */
public final class Utils {

    /**
     * @param string       string representation of a int which is to be parsed and read from
     * @param defaultValue in case parsing fails or string is null, returns this default value
     * @return int parsed value or the default value in case parsing failed
     */
    public static int readInt(String string, int defaultValue) {
        int returnValue = defaultValue;
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
        long returnValue = defaultValue;
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
        String requestURIExtension;
        if (requestURI.endsWith(EXT_JS)) {
            requestURIExtension = EXT_JS;
        } else if (requestURI.endsWith(EXT_JSON)) {
            requestURIExtension = EXT_JSON;
        } else if (requestURI.endsWith(EXT_CSS)) {
            requestURIExtension = EXT_CSS;
        } else {
            requestURIExtension = "";
        }
        return requestURIExtension;
    }

    /**
     *
     * @param extension - .js or .css etc.
     * @return - mime like text/javascript or text/css etc.
     */
    public static String selectMimeForExtension(String extension){
        if (EXT_JS.equals(extension)) {
            return MIME_JS;
        }else        if (EXT_CSS.equals(extension)) {
            return MIME_CSS;
        }else        if (EXT_JSON.equals(extension)) {
            return MIME_JSON;
        }
        return null;//"plain/text";
    }

    private Utils() {
    } //non instantiable

}
