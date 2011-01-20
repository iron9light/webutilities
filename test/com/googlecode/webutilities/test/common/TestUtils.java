package com.googlecode.webutilities.test.common;

import com.googlecode.webutilities.common.Constants;

import java.io.*   ;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class TestUtils {

    //HTTP dates are in one of these format
    //@see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html

    public static final String DATE_PATTERN_RFC_1123 = "EEE, dd MMM yyyyy HH:mm:ss z";

    public static final String DATE_PATTERN_RFC_1036 = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

    public static final String DATE_PATTERN_ANSI_C = "EEE MMM d HH:mm:ss yyyyy";

    public static final String DATE_PATTERN_HTTP_HEADER ="EEE, dd MMM yyyy HH:mm:ss zzz";

    //HTTP locale - US
    public final static Locale DEFAULT_LOCALE_US = Locale.US;

    //HTTP timeZone - GMT
    public final static TimeZone DEFAULT_ZONE_GMT = TimeZone.getTimeZone("GMT");



    private TestUtils() {
    }

    public static String readContents(InputStream inputStream) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        inputStream.close();
        return stringBuilder.toString();

    }

    /**
     *
     * @param headerDateString - from request header
     * @return
     * @throws java.text.ParseException
     */
    public static Date readDateFromHeader(String headerDateString){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_HTTP_HEADER, DEFAULT_LOCALE_US);
        try{
            return simpleDateFormat.parse(headerDateString);
        }catch (Exception e){}

        //try another rfc1123
        simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_RFC_1123,DEFAULT_LOCALE_US);
        try{
            return simpleDateFormat.parse(headerDateString);
        }catch (Exception e){}

        //try another rfc1036
        simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_RFC_1036,DEFAULT_LOCALE_US);
        try{
            return simpleDateFormat.parse(headerDateString);
        }catch (Exception e){}

        //try another ansi
        simpleDateFormat = new SimpleDateFormat(DATE_PATTERN_ANSI_C,DEFAULT_LOCALE_US);
        try{
            return simpleDateFormat.parse(headerDateString);
        }catch (Exception e){}

        return null;
    }

    public static boolean contentEquals(InputStream streamLeft, InputStream streamRight) throws IOException {
        if (!(streamLeft instanceof BufferedInputStream)) {
            streamLeft = new BufferedInputStream(streamLeft);
        }
        if (!(streamRight instanceof BufferedInputStream)) {
            streamRight = new BufferedInputStream(streamRight);
        }
        int ch = streamLeft.read();
        while (-1 != ch) {
            int ch2 = streamRight.read();
            if (ch != ch2) {
                return false;
            }
            ch = streamLeft.read();
        }

        int ch2 = streamRight.read();
        return (ch2 == -1);
    }
}
