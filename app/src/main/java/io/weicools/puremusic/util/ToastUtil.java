package io.weicools.puremusic.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Author: weicools
 * Time: 2017/10/30 下午4:59
 */

public class ToastUtil {
    public static void showShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
