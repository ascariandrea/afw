package com.ascariandrea.moai.utils;

/**
 * Created by andreaascari on 19/03/14.
 */
public class IntegerUtils {
    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;

    }

}
