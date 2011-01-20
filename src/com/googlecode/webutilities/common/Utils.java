package com.googlecode.webutilities.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
        if (requestURI.endsWith(Constants.EXT_JS)) {
            requestURIExtension = Constants.EXT_JS;
        } else if (requestURI.endsWith(Constants.EXT_JSON)) {
            requestURIExtension = Constants.EXT_JSON;
        } else if (requestURI.endsWith(Constants.EXT_CSS)) {
            requestURIExtension = Constants.EXT_CSS;
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
        if (Constants.EXT_JS.equals(extension)) {
            return Constants.MIME_JS;
        }else        if (Constants.EXT_CSS.equals(extension)) {
            return Constants.MIME_CSS;
        }else        if (Constants.EXT_JSON.equals(extension)) {
            return Constants.MIME_JSON;
        }
        return null;//"plain/text";
    }

    private Utils() {
    } //non instantiable

//    public static void main(String[] args) throws Exception{
//        System.out.println(readDateFromHeader("Thu, 20 Jan 2011 16:11:23 GMT"));
//    }
}
