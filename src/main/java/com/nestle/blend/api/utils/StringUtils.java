package com.nestle.blend.api.utils;

import java.security.SecureRandom;
import java.text.DecimalFormat;

public class StringUtils {
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";

    public static boolean checkNotEmpty(String val) {
        if (val == null || val.equalsIgnoreCase("null")) {
            return false;
        }

        return val.length() > 0;
    }

    public static String decimalFormat(Object obj){
        DecimalFormat df = new DecimalFormat("#,##0.00");

        return df.format(obj);
    }

    public static String randomString(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    public static String randomNumber(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(NUMERIC.length())));
        }
        return sb.toString();
    }
}
