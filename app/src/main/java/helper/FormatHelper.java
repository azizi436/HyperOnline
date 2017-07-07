/*
 * Copyright (c) 2017 - All Rights Reserved - Arash Hatami
 */

package helper;

public class FormatHelper {
    // define persian numbers for replacing old ones
    private static String[] persianNumbers = new String[]{"۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"};

    public static String toPersianNumber(String text) {
        if (text.isEmpty())
            return "";
        String out = "";
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if ('0' <= c && c <= '9') {
                int number = Integer.parseInt(String.valueOf(c));
                out += persianNumbers[number];
            } else if (c == '٫') {
                out += '،';
            } else {
                out += c;
            }
        }
        return out;
    }

    public static String toEnglishNumber(String text) {
        if (text.isEmpty()) return "";
        String out = "";
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '۰') out += '0';
            if (c == '۱') out += '1';
            if (c == '۲') out += '2';
            if (c == '۳') out += '3';
            if (c == '۴') out += '4';
            if (c == '۵') out += '5';
            if (c == '۶') out += '6';
            if (c == '۷') out += '7';
            if (c == '۸') out += '8';
            if (c == '۹') out += '9';
            if (c == '،') out += ',';
        }
        return out;
    }
}
