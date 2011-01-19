package com.googlecode.webutilities.test;

import java.io.*   ;

public final class TestUtils {

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
