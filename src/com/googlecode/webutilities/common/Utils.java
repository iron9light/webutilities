package com.googlecode.webutilities.common;

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

    private Utils() {
    } //non instantiable

}
