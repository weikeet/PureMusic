package io.weicools.puremusic.util;

/**
 * Author: weicools
 * Time: 2017/10/30 下午7:09
 */

public class ParseUtil {
    public static long parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
